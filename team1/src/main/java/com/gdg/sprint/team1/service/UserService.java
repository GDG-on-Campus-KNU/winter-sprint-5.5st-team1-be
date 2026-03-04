package com.gdg.sprint.team1.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.dto.my.UpdateMyInfoRequest;
import com.gdg.sprint.team1.entity.User;
import com.gdg.sprint.team1.exception.UserNotFoundException;
import com.gdg.sprint.team1.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findById(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateMyInfo(Integer userId, UpdateMyInfoRequest request) {
        if (request == null || !request.hasAnyField()) {
            throw new IllegalArgumentException("수정할 필드가 없습니다.");
        }

        User user = findById(userId);
        user.updateProfile(request.name(), request.phone(), request.address());
        return user;
    }
}
