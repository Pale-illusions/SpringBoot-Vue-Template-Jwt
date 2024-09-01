package com.iflove.config;

import cn.hutool.jwt.JWTUtil;
import com.iflove.entity.Const;
import com.iflove.entity.RestBean;
import com.iflove.entity.vo.response.AuthorizeVO;
import com.iflove.security.JwtAuthenticationProvider;
import com.iflove.security.JwtAuthenticationTokenFilter;
import com.iflove.security.UserDetailsImpl;
import com.iflove.utils.RedisUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfiguration {
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private UserDetailsService userDetailsService;
    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider();
    }
    // 自定义拦截器
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(conf -> conf
                        // 对登录注册允许匿名访问
                        .requestMatchers("/api/auth/**").permitAll()
                        // 放行错误页面
                        .requestMatchers("/error/**").permitAll()
                        // 测试
                        .requestMatchers("/test").permitAll()
                        // 接口文档
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 除上面外的所有请求全部需要鉴权认证
                        .anyRequest().authenticated()
                )
                // 登录
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(this::onAuthenticationSuccess)
                        .failureHandler(this::onAuthenticationFailure)
                )
                // 登出
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 异常处理
                .exceptionHandling(conf -> conf
                        .accessDeniedHandler(this::onAccessDeny)
                        .authenticationEntryPoint(this::onUnauthorized)
                )
                // 禁用缓存
                .headers(headersConfigurer -> headersConfigurer
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                )
                // 基于 token 不需要 csrf 防护
                .csrf(CsrfConfigurer::disable)
                // 基于token，所以不需要session
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 添加 JWT filter
                .addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                // 使用自定义 Provider
                .authenticationProvider(jwtAuthenticationProvider())
                .build();
    }

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 获得登陆成功的用户信息，保存到token中，响应中返回用户基本信息
        log.info("我是验证方法");
        String username = (String) authentication.getPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
        String uuid = UUID.randomUUID().toString();
        Date expireTime = redisUtil.expireTime();
        Map<String, Object> map = new HashMap<>() {
            {
                put("jwt_id", uuid);
                put("id", userDetails.getId());
                put("username", userDetails.getUsername());
                put("authorities", userDetails.getAuthorities());
                put("expire_time", expireTime);
            }
        };
        String token = JWTUtil.createToken(map, Const.JWT_SIGN_KEY.getBytes());
        // 将 token的 uuid 存入redis中
        redisUtil.set(uuid, expireTime.getTime() - new Date().getTime());
        // response返回数据
        AuthorizeVO vo = new AuthorizeVO();
        vo.setExpire((Date) map.get("expire_time"));
        vo.setRoles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        vo.setToken(token);
        vo.setUsername((String) map.get("username"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.success(vo).asJSONString());
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.failure(401, exception.getMessage()).asJSONString());
    }

    public void onAccessDeny(HttpServletRequest request,
                               HttpServletResponse response,
                               AccessDeniedException exception) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.forbidden(exception.getMessage()).asJSONString());
    }

    public void onUnauthorized(HttpServletRequest request,
                                HttpServletResponse response,
                                AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJSONString());
    }

    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        String token = request.getHeader("Authorization").substring(7);
        // 根据 uuid 删除
        if (redisUtil.delete((String) JWTUtil.parseToken(token).getPayload("jwt_id"))) {
            writer.write(RestBean.success().asJSONString());
        } else {
            writer.write(RestBean.failure(400, "退出登陆失败").asJSONString());
        }
    }
}
