package com.smk.presensi.service;

import com.smk.presensi.dto.lokasi.LokasiKantorRequest;
import com.smk.presensi.dto.lokasi.LokasiKantorResponse;
import com.smk.presensi.entity.LokasiKantor;
import com.smk.presensi.repository.LokasiKantorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk business logic CRUD LokasiKantor.
 * 
 * Business Rules:
 * - Nama lokasi harus unique (case-insensitive)
 * - Latitude: -90 to +90 degrees
 * - Longitude: -180 to +180 degrees
 * - Radius: minimum 10 meters (realistic value)
 * - Only 1 location can be active at a time
 * 
 * @author Copilot Assistant
 * @since Tahap 8 (Geolocation Validation)
 */
@Service
public class LokasiKantorService {
    
    @Autowired
    private LokasiKantorRepository lokasiKantorRepository;
    
    /**
     * Get all locations (active and inactive).
     * 
     * Use case:
     * - Admin dashboard: show all registered locations
     * - Allow admin to manage multiple locations (for multi-campus)
     * 
     * @return List of all locations
     */
    public List<LokasiKantorResponse> getAll() {
        return lokasiKantorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get location by ID.
     * 
     * Use case:
     * - View detail
     * - Pre-fill edit form
     * 
     * @param id Location ID
     * @return Location details
     * @throws RuntimeException if not found
     */
    public LokasiKantorResponse getById(Long id) {
        LokasiKantor lokasi = lokasiKantorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lokasi kantor tidak ditemukan dengan ID: " + id));
        return toResponse(lokasi);
    }
    
    /**
     * Create new location.
     * 
     * Validations:
     * 1. Check nama duplicate (case-insensitive)
     * 2. Validate latitude range (-90 to +90)
     * 3. Validate longitude range (-180 to +180)
     * 4. Validate radius minimum (10 meters)
     * 5. Default isActive = false (admin must activate manually)
     * 
     * Example:
     * - nama: "SMK Negeri 1 Jakarta"
     * - latitude: -6.200000
     * - longitude: 106.816666
     * - radiusValidasi: 200
     * 
     * @param request Location data
     * @return Created location
     * @throws RuntimeException if validation fails
     */
    @Transactional
    public LokasiKantorResponse create(LokasiKantorRequest request) {
        // 1. Validate: nama duplicate
        if (lokasiKantorRepository.findByNamaIgnoreCase(request.nama()).isPresent()) {
            throw new RuntimeException("Lokasi dengan nama '" + request.nama() + "' sudah terdaftar");
        }
        
        // 2. Validate: latitude range
        if (request.latitude() < -90 || request.latitude() > 90) {
            throw new RuntimeException("Latitude harus antara -90 hingga +90 degrees");
        }
        
        // 3. Validate: longitude range
        if (request.longitude() < -180 || request.longitude() > 180) {
            throw new RuntimeException("Longitude harus antara -180 hingga +180 degrees");
        }
        
        // 4. Validate: radius minimum
        if (request.radiusValidasi() < 10) {
            throw new RuntimeException("Radius validasi minimal 10 meter");
        }
        
        // 5. Create entity
        LokasiKantor lokasi = new LokasiKantor();
        lokasi.setNama(request.nama());
        lokasi.setLatitude(request.latitude());
        lokasi.setLongitude(request.longitude());
        lokasi.setRadiusValidasi(request.radiusValidasi());
        lokasi.setAlamat(request.alamat());
        lokasi.setKeterangan(request.keterangan());
        lokasi.setIsActive(false); // Default inactive (admin must activate)
        
        // 6. Save
        LokasiKantor saved = lokasiKantorRepository.save(lokasi);
        
        return toResponse(saved);
    }
    
    /**
     * Update existing location.
     * 
     * Logic:
     * - Can update all fields EXCEPT isActive (use activate() method)
     * - Same validations as create()
     * 
     * @param id Location ID
     * @param request Updated data
     * @return Updated location
     * @throws RuntimeException if not found or validation fails
     */
    @Transactional
    public LokasiKantorResponse update(Long id, LokasiKantorRequest request) {
        // 1. Get existing location
        LokasiKantor lokasi = lokasiKantorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lokasi kantor tidak ditemukan dengan ID: " + id));
        
        // 2. Validate: nama duplicate (exclude current)
        lokasiKantorRepository.findByNamaIgnoreCase(request.nama())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Lokasi dengan nama '" + request.nama() + "' sudah terdaftar");
                    }
                });
        
        // 3. Validate: latitude range
        if (request.latitude() < -90 || request.latitude() > 90) {
            throw new RuntimeException("Latitude harus antara -90 hingga +90 degrees");
        }
        
        // 4. Validate: longitude range
        if (request.longitude() < -180 || request.longitude() > 180) {
            throw new RuntimeException("Longitude harus antara -180 hingga +180 degrees");
        }
        
        // 5. Validate: radius minimum
        if (request.radiusValidasi() < 10) {
            throw new RuntimeException("Radius validasi minimal 10 meter");
        }
        
        // 6. Update fields
        lokasi.setNama(request.nama());
        lokasi.setLatitude(request.latitude());
        lokasi.setLongitude(request.longitude());
        lokasi.setRadiusValidasi(request.radiusValidasi());
        lokasi.setAlamat(request.alamat());
        lokasi.setKeterangan(request.keterangan());
        
        // 7. Save (updatedAt auto-updated by @PreUpdate)
        LokasiKantor updated = lokasiKantorRepository.save(lokasi);
        
        return toResponse(updated);
    }
    
    /**
     * Delete location.
     * 
     * Business rule:
     * - Cannot delete active location (must deactivate first)
     * - Prevent accidental deletion of currently used location
     * 
     * @param id Location ID
     * @throws RuntimeException if not found or is active
     */
    @Transactional
    public void delete(Long id) {
        // 1. Get location
        LokasiKantor lokasi = lokasiKantorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lokasi kantor tidak ditemukan dengan ID: " + id));
        
        // 2. Validate: cannot delete active location
        if (lokasi.getIsActive()) {
            throw new RuntimeException("Tidak dapat menghapus lokasi yang sedang aktif. " +
                    "Silakan aktifkan lokasi lain terlebih dahulu.");
        }
        
        // 3. Delete
        lokasiKantorRepository.delete(lokasi);
    }
    
    /**
     * Activate location (set as active).
     * 
     * Business rule:
     * - Only 1 location can be active at a time
     * - Automatically deactivate all other locations
     * - GPS validation will use this active location
     * 
     * Example:
     * - Before: SMK Campus A (active), SMK Campus B (inactive)
     * - Call activate(Campus B)
     * - After: SMK Campus A (inactive), SMK Campus B (active)
     * 
     * @param id Location ID to activate
     * @return Activated location
     * @throws RuntimeException if not found
     */
    @Transactional
    public LokasiKantorResponse activate(Long id) {
        // 1. Get location to activate
        LokasiKantor lokasi = lokasiKantorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lokasi kantor tidak ditemukan dengan ID: " + id));
        
        // 2. Deactivate all other locations
        List<LokasiKantor> allActive = lokasiKantorRepository.findByIsActive(true);
        for (LokasiKantor active : allActive) {
            if (!active.getId().equals(id)) {
                active.setIsActive(false);
                lokasiKantorRepository.save(active);
            }
        }
        
        // 3. Activate this location
        lokasi.setIsActive(true);
        LokasiKantor activated = lokasiKantorRepository.save(lokasi);
        
        return toResponse(activated);
    }
    
    /**
     * Get active location.
     * 
     * Use case:
     * - Display to user: "Radius validasi: 200m dari SMK Example"
     * - Check if GPS validation is enabled
     * 
     * @return Active location or null
     */
    public LokasiKantorResponse getActiveLocation() {
        return lokasiKantorRepository.findFirstByIsActive(true)
                .map(this::toResponse)
                .orElse(null);
    }
    
    /**
     * Convert Entity → DTO.
     */
    private LokasiKantorResponse toResponse(LokasiKantor lokasi) {
        return new LokasiKantorResponse(
                lokasi.getId(),
                lokasi.getNama(),
                lokasi.getLatitude(),
                lokasi.getLongitude(),
                lokasi.getRadiusValidasi(),
                lokasi.getIsActive(),
                lokasi.getAlamat(),
                lokasi.getKeterangan(),
                lokasi.getCreatedAt(),
                lokasi.getUpdatedAt(),
                formatCoordinates(lokasi.getLatitude(), lokasi.getLongitude())
        );
    }
    
    /**
     * Format coordinates for display.
     * 
     * Example:
     * - Input: -6.200000, 106.816666
     * - Output: "6°12'S, 106°49'E"
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return Formatted string
     */
    private String formatCoordinates(Double lat, Double lon) {
        String latDir = lat >= 0 ? "N" : "S";
        String lonDir = lon >= 0 ? "E" : "W";
        
        double latAbs = Math.abs(lat);
        double lonAbs = Math.abs(lon);
        
        int latDeg = (int) latAbs;
        int latMin = (int) ((latAbs - latDeg) * 60);
        
        int lonDeg = (int) lonAbs;
        int lonMin = (int) ((lonAbs - lonDeg) * 60);
        
        return String.format("%d°%d'%s, %d°%d'%s", 
                latDeg, latMin, latDir, 
                lonDeg, lonMin, lonDir);
    }
}
