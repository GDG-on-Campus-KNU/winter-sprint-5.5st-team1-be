package com.gdg.sprint.team1.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Integer userId) {
        super("존재하지 않는 사용자입니다. id=" + userId);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
