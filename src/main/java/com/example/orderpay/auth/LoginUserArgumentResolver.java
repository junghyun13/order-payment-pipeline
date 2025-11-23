package com.example.orderpay.auth;

import com.example.orderpay.member.User;
import com.example.orderpay.member.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class) &&
                parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        return userRepository.findByUsername(username)
                .orElse(null);
    }
}
