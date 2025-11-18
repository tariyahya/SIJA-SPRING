package com.smk.presensi.dto;

import java.util.Set;

/**
 * DTO untuk response User (GET)
 */
public record UserResponse(
    Long id,
    String username,
    String email,
    boolean enabled,
    Set<String> roles,  // Set of role names: ["ROLE_ADMIN", "ROLE_GURU"]
    Long siswaId,       // optional
    Long guruId         // optional
) {}
