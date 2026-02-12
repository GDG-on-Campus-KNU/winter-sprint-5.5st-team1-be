package com.gdg.sprint.team1.exception;

public class StoreNotFoundException extends RuntimeException {

    public StoreNotFoundException(Long storeId) {
        super("존재하지 않는 상점입니다. id=" + storeId);
    }
}
