package com.smk.presensi.dto.presensi;

import com.smk.presensi.enums.TipeUser;
import jakarta.validation.constraints.NotNull;

/**
 * DTO REQUEST untuk Checkin - Data yang dikirim client saat checkin.
 */
public record CheckinRequest(
        @NotNull(message = "Tipe user tidak boleh kosong")
        TipeUser tipe,

        Double latitude,
        Double longitude,
        String keterangan
) {
}
