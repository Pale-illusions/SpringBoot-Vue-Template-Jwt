package com.iflove.security;

import com.iflove.entity.dto.Account;
import com.iflove.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountService service;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account user = service.getUserByName(username);
        if (Objects.isNull(user)) throw new UsernameNotFoundException("用户不存在");
        return new UserDetailsImpl(user);
    }
}
