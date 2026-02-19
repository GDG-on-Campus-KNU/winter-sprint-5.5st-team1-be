package com.gdg.sprint.team1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "default-secret-change-in-production-min-32-chars-for-hs256";
    private long accessExpireMinutes = 30L;
    private long refreshExpireDays = 7L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessExpireMinutes() {
        return accessExpireMinutes;
    }

    public void setAccessExpireMinutes(long accessExpireMinutes) {
        this.accessExpireMinutes = accessExpireMinutes;
    }

    public long getRefreshExpireDays() {
        return refreshExpireDays;
    }

    public void setRefreshExpireDays(long refreshExpireDays) {
        this.refreshExpireDays = refreshExpireDays;
    }
}
