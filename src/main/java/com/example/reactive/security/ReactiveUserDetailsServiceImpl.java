package com.example.reactive.security;

import com.example.reactive.model.Authority;
import com.example.reactive.model.User;
import com.example.reactive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author duc-d
 */
@Component
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String login) {
        return userRepository.findByName(login)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new BadCredentialsException(String.format("User %s not found in database", login))))
                .map(this::createSpringSecurityUser);
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(User user) {
        Set<Authority> authorities = new HashSet<>();
        Authority adminAuthority = new Authority();
        adminAuthority.setName(AuthoritiesConstants.ANONYMOUS);
        authorities.add(adminAuthority);

        List<GrantedAuthority> grantedAuthorities = authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                grantedAuthorities);
    }
}
