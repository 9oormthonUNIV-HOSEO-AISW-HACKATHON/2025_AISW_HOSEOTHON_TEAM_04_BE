package com.example.familyq.domain.user.dto;

public record ChangePasswordRequest (
  String oldPassword,
  String newPassword
){}