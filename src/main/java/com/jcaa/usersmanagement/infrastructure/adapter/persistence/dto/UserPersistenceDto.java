package com.jcaa.usersmanagement.infrastructure.adapter.persistence.dto;

public record UserPersistenceDto(
    String id,
    String username,
    String name,
    String email,
    String password,
    String role,
    String status,
    String createdAt,
    String updatedAt) {}
