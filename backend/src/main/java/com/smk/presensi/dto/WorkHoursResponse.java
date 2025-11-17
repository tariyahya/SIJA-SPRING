package com.smk.presensi.dto;

public record WorkHoursResponse(
        Long presensiId,
        String username,
        String tipe,
        String tanggal,
        String jamMasuk,
        String jamPulang,
        long totalMinutes,
        long hours,
        long minutes,
        boolean isOvertime,
        String status
) {}
