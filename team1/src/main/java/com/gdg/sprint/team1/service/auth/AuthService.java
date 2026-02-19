package com.gdg.sprint.team1.service.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.config.JwtProperties;
import com.gdg.sprint.team1.dto.auth.LoginRequest;
import com.gdg.sprint.team1.dto.auth.LoginResponse;
import com.gdg.sprint.team1.dto.auth.SignupRequest;
import com.gdg.sprint.team1.entity.RefreshToken;
import com.gdg.sprint.team1.entity.User;
import com.gdg.sprint.team1.exception.DuplicateEmailException;
import com.gdg.sprint.team1.exception.InvalidRefreshTokenException;
import com.gdg.sprint.team1.exception.LoginFailedException;
import com.gdg.sprint.team1.repository.RefreshTokenRepository;
import com.gdg.sprint.team1.security.JwtTokenProvider;
import com.gdg.sprint.team1.security.JwtTokenProvider.TokenPayload;
import com.gdg.sprint.team1.service.user.UserService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public User signup(SignupRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = User.create(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.name(),
            request.phone(),
            request.address()
        );
        return userService.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.email())
            .orElseThrow(LoginFailedException::new);

        String stored = user.getPassword();
        boolean matches = (stored != null && stored.startsWith("$2"))
            ? passwordEncoder.matches(request.password(), stored)
            : (request.password() != null && request.password().equals(stored));
        if (!matches) {
            throw new LoginFailedException();
        }

        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getRole());
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtProperties.getRefreshExpireDays());
        refreshTokenRepository.save(new RefreshToken(user, refreshTokenValue, expiresAt));
        return LoginResponse.of(accessToken, refreshTokenValue);
    }

    @Transactional
    public LoginResponse refresh(String refreshTokenValue) {
        TokenPayload payload;
        try {
            payload = jwtTokenProvider.parseToken(refreshTokenValue);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(InvalidRefreshTokenException::new);
        refreshTokenRepository.deleteByToken(refreshTokenValue);

        User user = userService.findById(payload.userId());

        String newAccessToken = jwtTokenProvider.createToken(user.getId(), user.getRole());
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtProperties.getRefreshExpireDays());
        refreshTokenRepository.save(new RefreshToken(user, newRefreshTokenValue, expiresAt));
        return LoginResponse.of(newAccessToken, newRefreshTokenValue);
    }
}
