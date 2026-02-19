package com.gdg.sprint.team1.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.gdg.sprint.team1.exception.AuthRequiredException;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && UserContextHolder.UserContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        CurrentUser ann = parameter.getParameterAnnotation(CurrentUser.class);
        if (ann == null) {
            return null;
        }
        UserContextHolder.UserContext context = UserContextHolder.get();
        if (ann.required() && context == null) {
            throw new AuthRequiredException();
        }
        return context;
    }
}
