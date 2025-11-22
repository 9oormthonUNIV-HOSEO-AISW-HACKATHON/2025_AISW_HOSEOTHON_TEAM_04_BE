package com.example.familyq.global.security;

import com.example.familyq.domain.user.dto.LoginUserInfo;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean hasType = LoginUserInfo.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && hasType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        if (request == null || response == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            // 유효하지 않은 세션의 경우 쿠키 정리
            clearSessionCookie(response);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LoginUserInfo loginUser = (LoginUserInfo) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            // 세션은 있지만 로그인 정보가 없는 경우도 쿠키 정리
            clearSessionCookie(response);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    private void clearSessionCookie(HttpServletResponse response) {
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
    }
}
