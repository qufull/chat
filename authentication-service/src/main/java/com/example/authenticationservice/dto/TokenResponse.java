package com.example.authenticationservice.dto;

public record TokenResponse(String accessToken, long expireIn, String tokenType,String sid) {
}
