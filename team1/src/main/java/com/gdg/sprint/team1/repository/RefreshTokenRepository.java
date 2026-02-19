package com.gdg.sprint.team1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdg.sprint.team1.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(Integer userId);
}
