package com.smk.presensi.service;

import com.smk.presensi.entity.LokasiKantor;
import com.smk.presensi.repository.LokasiKantorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service untuk validasi geolocation (GPS) menggunakan Haversine formula.
 * 
 * Haversine formula digunakan untuk menghitung jarak antara 2 koordinat
 * di permukaan bumi (sphere). Formula ini memperhitungkan kelengkungan bumi.
 * 
 * Formula:
 * a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
 * c = 2 ⋅ atan2( √a, √(1−a) )
 * d = R ⋅ c
 * 
 * Where:
 * - φ = latitude (in radians)
 * - λ = longitude (in radians)
 * - R = Earth radius (6371 km)
 * - d = distance (in km)
 * 
 * @author Copilot Assistant
 * @since Tahap 8 (Geolocation Validation)
 */
@Service
public class GeolocationService {
    
    /**
     * Earth radius in kilometers.
     * Mean radius: 6371 km (at equator: 6378 km, at poles: 6357 km)
     */
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    @Autowired
    private LokasiKantorRepository lokasiKantorRepository;
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula.
     * 
     * Steps:
     * 1. Convert degrees to radians
     * 2. Calculate differences (Δφ, Δλ)
     * 3. Apply Haversine formula
     * 4. Return distance in meters
     * 
     * Example:
     * - Point A: -6.200000, 106.816666 (Jakarta Pusat)
     * - Point B: -6.201000, 106.817666 (1km away)
     * - Distance: ~157 meters
     * 
     * @param lat1 Latitude point 1 (degrees)
     * @param lon1 Longitude point 1 (degrees)
     * @param lat2 Latitude point 2 (degrees)
     * @param lon2 Longitude point 2 (degrees)
     * @return Distance in METERS (double)
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // Validate input
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            throw new IllegalArgumentException("Latitude dan Longitude tidak boleh null");
        }
        
        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Calculate differences
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        
        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Distance in kilometers
        double distanceKm = EARTH_RADIUS_KM * c;
        
        // Convert to meters
        return distanceKm * 1000;
    }
    
    /**
     * Check if user GPS coordinates are within school radius.
     * 
     * Logic:
     * 1. Get active school location from database
     * 2. If no active location → return true (skip validation)
     * 3. Calculate distance using Haversine
     * 4. Compare with radius
     * 
     * Example:
     * - User: -6.200500, 106.816666
     * - School: -6.200000, 106.816666, radius=200m
     * - Distance: ~55 meters
     * - Result: true (within radius)
     * 
     * @param userLat User latitude
     * @param userLon User longitude
     * @return true if within radius OR no active location, false if too far
     */
    public boolean isWithinRadius(Double userLat, Double userLon) {
        // Get active school location
        Optional<LokasiKantor> lokasiOpt = lokasiKantorRepository.findFirstByIsActive(true);
        
        // If no active location, skip validation (return true)
        if (lokasiOpt.isEmpty()) {
            return true; // No geolocation validation configured
        }
        
        LokasiKantor lokasi = lokasiOpt.get();
        
        // Calculate distance
        double distance = calculateDistance(
            userLat, userLon,
            lokasi.getLatitude(), lokasi.getLongitude()
        );
        
        // Check if within radius
        return distance <= lokasi.getRadiusValidasi();
    }
    
    /**
     * Validate user GPS and throw exception if too far.
     * 
     * Use case:
     * - Called from PresensiService.checkin() for MANUAL method
     * - Throw exception dengan informasi jarak untuk user feedback
     * 
     * Example exception message:
     * "Lokasi terlalu jauh dari sekolah. Jarak: 523 meter (maksimal: 200 meter)"
     * 
     * @param userLat User latitude
     * @param userLon User longitude
     * @throws RuntimeException if too far from school
     */
    public void validateLocation(Double userLat, Double userLon) {
        // Validate input
        if (userLat == null || userLon == null) {
            throw new IllegalArgumentException("Latitude dan Longitude wajib diisi untuk checkin manual");
        }
        
        // Get active school location
        Optional<LokasiKantor> lokasiOpt = lokasiKantorRepository.findFirstByIsActive(true);
        
        // If no active location, skip validation
        if (lokasiOpt.isEmpty()) {
            return; // No geolocation validation configured
        }
        
        LokasiKantor lokasi = lokasiOpt.get();
        
        // Calculate distance
        double distance = calculateDistance(
            userLat, userLon,
            lokasi.getLatitude(), lokasi.getLongitude()
        );
        
        // Validate radius
        if (distance > lokasi.getRadiusValidasi()) {
            throw new RuntimeException(String.format(
                "Lokasi terlalu jauh dari %s. Jarak: %.0f meter (maksimal: %d meter). " +
                "Pastikan Anda berada di area sekolah saat checkin.",
                lokasi.getNama(),
                distance,
                lokasi.getRadiusValidasi()
            ));
        }
    }
    
    /**
     * Get active school location details.
     * 
     * Use case:
     * - Display to user: "Radius validasi: 200m dari SMK Example"
     * - Check if geolocation feature is enabled
     * 
     * @return Optional<LokasiKantor> - active location or empty
     */
    public Optional<LokasiKantor> getActiveLocation() {
        return lokasiKantorRepository.findFirstByIsActive(true);
    }
    
    /**
     * Format distance untuk display (with unit).
     * 
     * Example:
     * - 50 → "50 meter"
     * - 523 → "523 meter"
     * - 1200 → "1.2 km"
     * 
     * @param distanceInMeters Distance in meters
     * @return Formatted string
     */
    public String formatDistance(double distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format("%.0f meter", distanceInMeters);
        } else {
            return String.format("%.1f km", distanceInMeters / 1000);
        }
    }
}
