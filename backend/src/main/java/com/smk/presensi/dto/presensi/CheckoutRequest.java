package com.smk.presensi.dto.presensi;

/**
 * DTO REQUEST untuk Checkout - Data yang dikirim client saat checkout.
 */
public record CheckoutRequest(
        Double latitude,
        Double longitude,
        String keterangan
) {
}
