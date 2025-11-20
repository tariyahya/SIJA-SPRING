package com.smk.presensi.controller;

import com.smk.presensi.dto.MapelRequest;
import com.smk.presensi.dto.MapelResponse;
import com.smk.presensi.service.MapelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mapel")
public class MapelController {

    private final MapelService mapelService;

    public MapelController(MapelService mapelService) {
        this.mapelService = mapelService;
    }

    /**
     * GET /api/mapel
     * Access: ADMIN atau GURU (read)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GURU')")
    public List<MapelResponse> list(@RequestParam(value = "q", required = false) String q) {
        return mapelService.search(q);
    }

    /**
     * GET /api/mapel/{id}
     * Access: ADMIN atau GURU (read)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GURU')")
    public MapelResponse getById(@PathVariable Long id) {
        return mapelService.findById(id);
    }

    /**
     * POST /api/mapel
     * Access: ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MapelResponse> create(@Valid @RequestBody MapelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapelService.create(request));
    }

    /**
     * PUT /api/mapel/{id}
     * Access: ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MapelResponse update(@PathVariable Long id, @Valid @RequestBody MapelRequest request) {
        return mapelService.update(id, request);
    }

    /**
     * DELETE /api/mapel/{id}
     * Access: ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mapelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

