package com.gdg.sprint.team1.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "users")
public class User {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter
    @Column(nullable = false)
    private String password;

    @Getter
    @Column(nullable = false, length = 100)
    private String name;

    @Getter
    @Column(length = 20)
    private String phone;

    @Getter
    @Column(columnDefinition = "TEXT")
    private String address;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role = UserRole.USER;

    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected User() {}

    public static User create(
        String email,
        String encodedPassword,
        String name,
        String phone,
        String address
    ) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.phone = phone;
        user.address = address;
        user.role = UserRole.USER;
        return user;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setId(Integer id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(UserRole role) { this.role = role; }

    public void updateProfile(String name, String phone, String address) {
        if (name != null) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("이름은 공백만 입력할 수 없습니다.");
            }
            this.name = trimmedName;
        }
        if (phone != null) {
            String trimmedPhone = phone.trim();
            if (!trimmedPhone.isEmpty()) {
                this.phone = trimmedPhone;
            }
        }
        if (address != null) {
            String trimmedAddress = address.trim();
            if (!trimmedAddress.isEmpty()) {
                this.address = trimmedAddress;
            }
        }
    }

    public enum UserRole {
        USER,
        ADMIN
    }
}
