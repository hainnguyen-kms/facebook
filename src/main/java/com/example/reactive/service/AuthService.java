package com.example.reactive.service;


import com.example.reactive.model.User;
import com.example.reactive.repository.UserRepository;
import com.example.reactive.security.dto.LoginVM;
import com.example.reactive.security.jwt.JWTReactiveAuthenticationManager;
import com.example.reactive.security.jwt.JWTToken;
import com.example.reactive.security.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.Validator;

@Service
public class AuthService {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private JWTReactiveAuthenticationManager authenticationManager;
    @Autowired
    private Validator validation;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public Mono<ServerResponse> signin(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoginVM.class)
               .flatMap(loginVM -> {
                   if (!this.validation.validate(loginVM).isEmpty()) {
                       return Mono.error(new RuntimeException("Bad request"));
                   }
                   Authentication authenticationToken =
                           new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());
                   ReactiveSecurityContextHolder.withAuthentication(authenticationToken);


                   return this.authenticationManager.authenticate(authenticationToken)
                           .map(auth -> {
                               String jwt = tokenProvider.createToken(auth);
                               return new JWTToken(jwt);
                           })
                           .doOnError(throwable -> {
                               throw new BadCredentialsException("Bad credentials");
                           });
               })
                .flatMap(token -> ServerResponse.ok().body(BodyInserters.fromValue(token)));
    }

    public Mono<ServerResponse> signup(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoginVM.class)
                .map(signUp -> {
                    SignUpInfo signUpInfo = new SignUpInfo();
                    signUpInfo.setSignUpInfo(signUp);
                    return signUpInfo;
                })
                .flatMap(signUpInfo -> userRepository
                        .findByName(signUpInfo.getSignUpInfo().getUsername())
                        .map(signUpInfo::setFindUserResult)
                        .onErrorResume(err -> {
                            signUpInfo.setFindUserResult(null);
                            return Mono.just(signUpInfo);
                        })
                )
                .flatMap(signUpInfo -> {
                    if(signUpInfo.getFindUserResult() != null) throw new RuntimeException("user info exists, please login");
                    User newUser = new User();
                    newUser.setUsername(signUpInfo.getSignUpInfo().getUsername());
                    newUser.setPassword(this.passwordEncoder.encode(signUpInfo.getSignUpInfo().getPassword()));
                    return userRepository.createUser(newUser);
                })
                .flatMap(user -> ServerResponse.ok().body(BodyInserters.fromValue(user)));
    }

    static class SignUpInfo {
        private LoginVM signUpInfo;
        private User findUserResult;

        public LoginVM getSignUpInfo() {
            return signUpInfo;
        }

        public void setSignUpInfo(LoginVM signUpInfo) {
            this.signUpInfo = signUpInfo;
        }

        public User getFindUserResult() {
            return findUserResult;
        }

        public SignUpInfo setFindUserResult(User findUserResult) {
            this.findUserResult = findUserResult;
            return this;
        }
    }
}