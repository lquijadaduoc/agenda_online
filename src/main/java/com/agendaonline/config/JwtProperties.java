package com.agendaonline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenValidityMinutes;
    private long refreshTokenValidityDays;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenValidityMinutes() {
        return accessTokenValidityMinutes;
    }

    public void setAccessTokenValidityMinutes(long accessTokenValidityMinutes) {
        this.accessTokenValidityMinutes = accessTokenValidityMinutes;
    }

    public long getRefreshTokenValidityDays() {
        return refreshTokenValidityDays;
    }

    public void setRefreshTokenValidityDays(long refreshTokenValidityDays) {
        this.refreshTokenValidityDays = refreshTokenValidityDays;
    }
}
