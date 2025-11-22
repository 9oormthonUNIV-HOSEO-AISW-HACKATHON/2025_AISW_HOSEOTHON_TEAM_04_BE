package com.example.familyq.domain.user.dto;

import com.example.familyq.domain.user.entity.User;

public record UserResponse(
        Long Id,
        String userId,
        String name,
        String role,
        Integer year,
        Integer month,
        Integer day
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserId(),
                user.getName(),
                user.getRole(),
                user.getYear(),
                user.getMonth(),
                user.getDay()
        );
    }
}