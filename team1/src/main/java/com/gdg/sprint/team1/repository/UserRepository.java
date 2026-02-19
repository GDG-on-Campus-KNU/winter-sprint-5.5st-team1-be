package com.gdg.sprint.team1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdg.sprint.team1.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
