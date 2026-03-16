package com.money.kark_profit.transform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private Date expiresAt;        // When the token expires
    private long expiresIn;         // Seconds until expiration (useful for frontend)
    private String expiresInReadable; // Human readable format (e.g., "1 minute")

    // Optional: You can add a factory method for easy creation
    public static AuthResponse of(String token, String username, String email, Date expiresAt) {
        long expiresInMillis = expiresAt.getTime() - System.currentTimeMillis();
        long expiresInSeconds = expiresInMillis / 1000;

        return AuthResponse.builder()
                .token(token)
                .username(username)
                .email(email)
                .expiresAt(expiresAt)
                .expiresIn(expiresInSeconds)
                .expiresInReadable(formatExpiryTime(expiresInSeconds))
                .build();
    }

    private static String formatExpiryTime(long seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else {
            long days = seconds / 86400;
            return days + " day" + (days != 1 ? "s" : "");
        }
    }
}