package com.iflove.controller;

import com.iflove.entity.RestBean;
import com.iflove.entity.dto.Account;
import com.iflove.entity.vo.request.ConfirmResetVO;
import com.iflove.entity.vo.request.EmailRegisterVO;
import com.iflove.entity.vo.request.EmailResetVO;
import com.iflove.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@RestController
@RequestMapping("api/auth")
@Validated
public class AuthorizeController {
    @Resource
    AccountService accountService;

    @GetMapping("check-email")
    public RestBean<Boolean> checkEmail(@RequestParam("email") @Email String email) {
        return accountService.existsAccountByEmail(email)
                ? RestBean.success(true) : RestBean.failure(406, "该邮箱未被注册");
    }

    /**
     * 请求邮件验证码
     * @param email 邮箱地址
     * @param type 邮件类型
     * @param request 请求
     * @return 成功？
     */
    @GetMapping("ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam("email") @Email String email,
                                        @RequestParam("type") @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request) {
        return this.messageHandle(() ->
                accountService.registerEmailVerifyCode(type, email, request.getRemoteAddr()));
    }

    /**
     * 进行用户注册操作，需要先请求邮件验证码
     * @param vo 注册信息
     * @return 是否注册成功
     */
    @PostMapping("register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo) {
        return messageHandle(vo, accountService::registerEmailAccount);
    }

    /**
     * 执行密码重置确认，检查验证码是否正确
     * @param vo 密码重置信息
     * @return 是否操作成功
     */
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo) {
        return messageHandle(vo, accountService::resetConfirm);
    }

    /**
     * 执行密码重置操作
     * @param vo 密码重置信息
     * @return 是否操作成功
     */
    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo) {
        return messageHandle(vo, accountService::resetEmailAccountPassword);
    }

    /**
     * 针对于传入值为 VO对象 且返回值为String作为错误信息的方法进行统一处理
     * @param vo 数据封装对象
     * @param function 调用service方法
     * @return 响应结果
     * @param <T> 响应结果类型
     */
    private <T> RestBean<Void> messageHandle(T vo, Function<T, String> function) {
        return messageHandle(() -> function.apply(vo));
    }

    /**
     * 针对于返回值为String作为错误信息的方法进行统一处理
     * @param action 具体操作
     * @return 响应结果
     * @param <T> 响应结果类型
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        return Objects.isNull(message) ? RestBean.success() : RestBean.failure(400, message);
    }
}
