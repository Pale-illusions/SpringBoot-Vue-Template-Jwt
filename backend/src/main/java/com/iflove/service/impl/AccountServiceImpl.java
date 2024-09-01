package com.iflove.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iflove.entity.Const;
import com.iflove.entity.dto.Account;
import com.iflove.entity.dto.Role;
import com.iflove.entity.vo.request.ConfirmResetVO;
import com.iflove.entity.vo.request.EmailRegisterVO;
import com.iflove.entity.vo.request.EmailResetVO;
import com.iflove.mapper.AccountMapper;
import com.iflove.service.AccountService;
import com.iflove.utils.FlowUtil;
import com.iflove.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author 苍镜月
* @description 针对表【account】的数据库操作Service实现
* @createDate 2024-08-30 13:42:18
*/
@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService{
    @Resource
    private AccountMapper accountMapper;
    @Resource
    private AmqpTemplate amqpTemplate;
    @Resource
    private FlowUtil flowUtil;
    @Resource
    PasswordEncoder passwordEncoder;

    /**
     * 根据username获得用户对象
     * @param username 用户名
     * @return 用户对象
     */
    @Override
    public Account getUserByName(String username) {
        return accountMapper.getUserByName(username);
    }

    /**
     * 生成注册验证码存入Redis中，并将邮件发送请求提交到消息队列等待发送
     * @param type 类型
     * @param email 邮件地址
     * @param ip 请求ip地址
     * @return 操作结果，null表示正常，否则为错误原因
     */
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!verifyLimit(ip)) {
                return "请求频繁，请稍后再试";
            }
            int code = new Random().nextInt((int) (9 * 1e5)) + (int) 1e5;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);
            flowUtil.addVerifyCode(email, code);
            return null;
        }
    }

    @Transactional
    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String username = vo.getUsername();
        String code = flowUtil.getVerifyCode(email);
        if (Objects.isNull(code)) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码输入错误，请重新输入";
        if (this.existsAccountByEmail(email)) return "此电子邮件已被注册";
        if (this.existsAccountByUsername(username)) return "此用户名已被注册";
        String passsword = passwordEncoder.encode(vo.getPassword());
        String roleId = UUID.randomUUID().toString();
        if (accountMapper.saveUser(username, passsword, email, new Date(), roleId)) {
            if (accountMapper.saveRole(Const.ROLE_DEFAULT, roleId)) {
                flowUtil.deleteVerifyCode(email);
                return null;
            } else {
                return "内部错误，请联系管理员";
            }
        } else {
            return "内部错误，请联系管理员";
        }
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = flowUtil.getVerifyCode(email);
        if (Objects.isNull(code)) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String verify = this.resetConfirm(new ConfirmResetVO(vo.getEmail(), vo.getCode()));
        if (Objects.nonNull(verify)) return verify;
        String password = passwordEncoder.encode(vo.getPassword());
        boolean update = this.update().eq("email", vo.getEmail()).set("password", password).update();
        if(update) {
            flowUtil.deleteVerifyCode(vo.getEmail());
        }
        log.info("密码更新成功，新密码为：" + vo.getPassword());
        return update ? null : "更新失败，请联系管理员";
    }

    @Override
    public boolean existsAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    private boolean existsAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }

    /**
     * 针对 IP地址 进行邮箱验证码获取限流
     * @param address ip
     * @return 是否通过验证
     */
    private boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT + address;
        return flowUtil.limitEmailRequest(key, Const.verifyLimit);
    }
}




