package com.gdg.sprint.team1.service.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdg.sprint.team1.config.JwtProperties;
import com.gdg.sprint.team1.dto.auth.LoginRequest;
import com.gdg.sprint.team1.dto.auth.LoginResponse;
import com.gdg.sprint.team1.dto.auth.SignupRequest;
import com.gdg.sprint.team1.entity.RefreshToken;
import com.gdg.sprint.team1.entity.User;
import com.gdg.sprint.team1.entity.User.UserRole;
import com.gdg.sprint.team1.exception.DuplicateEmailException;
import com.gdg.sprint.team1.exception.InvalidRefreshTokenException;
import com.gdg.sprint.team1.exception.LoginFailedException;
import com.gdg.sprint.team1.exception.UserNotFoundException;
import com.gdg.sprint.team1.repository.RefreshTokenRepository;
import com.gdg.sprint.team1.repository.UserRepository;
import com.gdg.sprint.team1.security.JwtTokenProvider;
import com.gdg.sprint.team1.security.JwtTokenProvider.TokenPayload;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public User signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
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

        User user = userRepository.findById(payload.userId())
            .orElseThrow(InvalidRefreshTokenException::new);

        String newAccessToken = jwtTokenProvider.createToken(user.getId(), user.getRole());
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtProperties.getRefreshExpireDays());
        refreshTokenRepository.save(new RefreshToken(user, newRefreshTokenValue, expiresAt));
        return LoginResponse.of(newAccessToken, newRefreshTokenValue);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
