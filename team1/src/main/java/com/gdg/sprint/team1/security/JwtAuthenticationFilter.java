package com.gdg.sprint.team1.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.entity.User.UserRole;
import com.gdg.sprint.team1.exception.AuthExpiredException;
import com.gdg.sprint.team1.exception.AuthInvalidException;
import com.gdg.sprint.team1.exception.AuthRequiredException;
import com.gdg.sprint.team1.exception.ForbiddenException;

@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final List<String> PERMIT_PATHS = List.of(
        "/api/v1/auth/",
        "/api/v1/products",
        "/swagger-ui",
        "/v3/api-docs",
        "/api-docs"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PERMIT_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token == null || token.isBlank()) {
                sendAuthError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_REQUIRED", "인증이 필요합니다.");
                return;
            }

            JwtTokenProvider.TokenPayload payload = jwtTokenProvider.parseToken(token);
            UserContextHolder.set(new UserContextHolder.UserContext(payload.userId(), payload.role()));

            if (request.getRequestURI().startsWith("/api/v1/admin/")) {
                if (payload.role() != UserRole.ADMIN) {
                    sendAuthError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "관리자만 접근할 수 있습니다.");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (AuthExpiredException e) {
            sendAuthError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_EXPIRED", "토큰이 만료되었습니다.");
        } catch (AuthInvalidException e) {
            sendAuthError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_INVALID", "유효하지 않은 토큰입니다.");
        } catch (AuthRequiredException e) {
            sendAuthError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_REQUIRED", "인증이 필요합니다.");
        } catch (ForbiddenException e) {
            sendAuthError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", e.getMessage());
        } finally {
            UserContextHolder.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            return header.substring(BEARER.length()).trim();
        }
        return null;
    }

    private void sendAuthError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body = ApiResponse.failure(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
