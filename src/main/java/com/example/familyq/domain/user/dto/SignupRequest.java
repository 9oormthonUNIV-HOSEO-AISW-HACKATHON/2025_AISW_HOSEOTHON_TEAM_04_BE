package com.example.familyq.domain.user.dto;

public record SignupRequest(
        String userId,
        String password,
        String name,
        String role,
        Integer year,
        Integer month,
        Integer day
) {}