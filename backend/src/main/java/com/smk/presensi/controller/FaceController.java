package com.smk.presensi.controller;

import com.smk.presensi.dto.presensi.FaceCheckinRequest;
import com.smk.presensi.dto.presensi.FaceEnrollmentRequest;
import com.smk.presensi.dto.presensi.FaceEnrollmentResponse;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.entity.Guru;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.entity.User;
import com.smk.presensi.repository.GuruRepository;
import com.smk.presensi.repository.SiswaRepository;
import com.smk.presensi.repository.UserRepository;
import com.smk.presensi.service.FaceRecognitionService;
import com.smk.presensi.service.PresensiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * FACE CONTROLLER - REST endpoints untuk Face Recognition.
 * 
 * Endpoints:
 * 1. POST /api/presensi/face/enroll - Enrollment (daftar wajah)
 * 2. POST /api/presensi/face/checkin - Checkin via face recognition
 * 3. GET /api/presensi/face/test - Connectivity test
 * 
 * Public endpoints (no JWT required) karena:
 * - Face recognition biasanya dari kamera/hardware
 * - Hardware tidak bisa login
 * - Validation: face harus enrolled di database
 * 
 * @author SMK Teaching Team
 */
@RestController
@RequestMapping("/api/presensi/face")
public class FaceController {

    private final FaceRecognitionService faceRecognitionService;
    private final PresensiService presensiService;
    private final SiswaRepository siswaRepository;
    private final GuruRepository guruRepository;
    private final UserRepository userRepository;

    public FaceController(
            FaceRecognitionService faceRecognitionService,
            PresensiService presensiService,
            SiswaRepository siswaRepository,
            GuruRepository guruRepository,
            UserRepository userRepository
    ) {
        this.faceRecognitionService = faceRecognitionService;
        this.presensiService = presensiService;
        this.siswaRepository = siswaRepository;
        this.guruRepository = guruRepository;
        this.userRepository = userRepository;
    }

    /**
     * ENDPOINT TEST - Untuk testing koneksi.
     * 
     * GET /api/presensi/face/test
     * 
     * Response:
     * "Face Recognition endpoint is working!"
     * 
     * Use case:
     * - Test apakah endpoint accessible
     * - Test apakah whitelist di SecurityConfig sudah benar
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Face Recognition endpoint is working!");
    }

    /**
     * ENROLLMENT - Daftar wajah user.
     * 
     * POST /api/presensi/face/enroll
     * Body: { "userId": 2, "imageBase64": "data:image/jpeg;base64,..." }
     * 
     * Flow:
     * 1. Terima userId dan imageBase64
     * 2. Cari user di database (siswa atau guru)
     * 3. Generate face encoding dari image
     * 4. Save encoding ke database
     * 5. Return success response
     * 
     * Note:
     * - Admin/Operator yang jalankan enrollment
     * - Bisa di-trigger dari web admin panel
     * - User upload foto frontal, clear, good lighting
     * 
     * @param request FaceEnrollmentRequest (userId, imageBase64)
     * @return FaceEnrollmentResponse (sukses info)
     */
    @PostMapping("/enroll")
    public ResponseEntity<?> enroll(@Valid @RequestBody FaceEnrollmentRequest request) {
        try {
            Long userId = request.userId();
            String imageBase64 = request.imageBase64();

            // 1. Cari user berdasarkan userId
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User ID " + userId + " tidak ditemukan"));

            // 2. Generate face encoding
            String faceEncoding = faceRecognitionService.generateFaceEncoding(imageBase64);

            // 3. Save encoding ke Siswa atau Guru (tergantung tipe user)
            // Cek di tabel Siswa dulu
            Optional<Siswa> siswaOpt = siswaRepository.findByUser(user);
            if (siswaOpt.isPresent()) {
                Siswa siswa = siswaOpt.get();
                siswa.setFaceEncoding(faceEncoding);
                siswa.setFaceEnrolledAt(LocalDateTime.now());
                siswaRepository.save(siswa);
                
                return ResponseEntity.ok(new FaceEnrollmentResponse(
                        userId,
                        user.getUsername(),
                        faceEncoding.length(),
                        siswa.getFaceEnrolledAt(),
                        "Face berhasil di-enroll untuk siswa " + user.getUsername()
                ));
            }

            // Jika bukan siswa, cek di tabel Guru
            Optional<Guru> guruOpt = guruRepository.findByUser(user);
            if (guruOpt.isPresent()) {
                Guru guru = guruOpt.get();
                guru.setFaceEncoding(faceEncoding);
                guru.setFaceEnrolledAt(LocalDateTime.now());
                guruRepository.save(guru);
                
                return ResponseEntity.ok(new FaceEnrollmentResponse(
                        userId,
                        user.getUsername(),
                        faceEncoding.length(),
                        guru.getFaceEnrolledAt(),
                        "Face berhasil di-enroll untuk guru " + user.getUsername()
                ));
            }

            // Jika user bukan siswa dan bukan guru (mungkin admin)
            throw new RuntimeException("User " + user.getUsername() + " bukan siswa atau guru");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Enrollment gagal: " + e.getMessage());
        }
    }

    /**
     * CHECKIN VIA FACE RECOGNITION.
     * 
     * POST /api/presensi/face/checkin
     * Body: { "imageBase64": "data:image/jpeg;base64,..." }
     * 
     * Flow:
     * 1. Terima imageBase64 (foto wajah dari kamera)
     * 2. Generate encoding dari image
     * 3. Loop semua siswa & guru yang sudah enroll face
     * 4. Calculate similarity dengan setiap enrolled face
     * 5. Jika ada yang match (similarity > threshold):
     *    - User identified!
     *    - Call presensiService.checkinFace()
     *    - Create presensi with method = FACE
     * 6. Else:
     *    - Return "Face tidak dikenali"
     * 
     * Note:
     * - NO userId di request (sistem harus auto-detect)
     * - Public endpoint (no JWT)
     * - Response time: 1-3 detik (tergantung jumlah enrolled users)
     * 
     * @param request FaceCheckinRequest (imageBase64)
     * @return PresensiResponse atau error message
     */
    @PostMapping("/checkin")
    public ResponseEntity<?> checkin(@Valid @RequestBody FaceCheckinRequest request) {
        try {
            String imageBase64 = request.imageBase64();

            // 1. Generate encoding dari input image
            String inputEncoding = faceRecognitionService.generateFaceEncoding(imageBase64);

            // 2. Cari match di semua siswa yang sudah enroll face
            for (Siswa siswa : siswaRepository.findByFaceEncodingIsNotNull()) {
                double similarity = faceRecognitionService.calculateSimilarity(
                        inputEncoding,
                        siswa.getFaceEncoding()
                );

                if (faceRecognitionService.isMatch(similarity)) {
                    // MATCH! Checkin via PresensiService
                    PresensiResponse response = presensiService.checkinFace(siswa);
                    return ResponseEntity.ok(response);
                }
            }

            // 3. Jika tidak match di siswa, cari di guru
            for (Guru guru : guruRepository.findByFaceEncodingIsNotNull()) {
                double similarity = faceRecognitionService.calculateSimilarity(
                        inputEncoding,
                        guru.getFaceEncoding()
                );

                if (faceRecognitionService.isMatch(similarity)) {
                    // MATCH! Checkin via PresensiService
                    PresensiResponse response = presensiService.checkinFace(guru);
                    return ResponseEntity.ok(response);
                }
            }

            // 4. Jika tidak ada yang match
            return ResponseEntity.status(404).body(
                    "Face tidak dikenali. Pastikan sudah enrollment dan foto clear."
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Checkin gagal: " + e.getMessage());
        }
    }

    /**
     * CHECKOUT VIA FACE RECOGNITION.
     * 
     * POST /api/presensi/face/checkout
     * Body: { "imageBase64": "data:image/jpeg;base64,..." }
     * 
     * Flow sama dengan checkin, tapi panggil checkoutFace() instead of checkinFace().
     * 
     * @param request FaceCheckinRequest (imageBase64)
     * @return PresensiResponse dengan jamPulang terisi atau error message
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody FaceCheckinRequest request) {
        try {
            String imageBase64 = request.imageBase64();

            // 1. Generate encoding dari input image
            String inputEncoding = faceRecognitionService.generateFaceEncoding(imageBase64);

            // 2. Cari match di semua siswa yang sudah enroll face
            for (Siswa siswa : siswaRepository.findByFaceEncodingIsNotNull()) {
                double similarity = faceRecognitionService.calculateSimilarity(
                        inputEncoding,
                        siswa.getFaceEncoding()
                );

                if (faceRecognitionService.isMatch(similarity)) {
                    // MATCH! Checkout via PresensiService
                    PresensiResponse response = presensiService.checkoutFace(siswa);
                    return ResponseEntity.ok(response);
                }
            }

            // 3. Jika tidak match di siswa, cari di guru
            for (Guru guru : guruRepository.findByFaceEncodingIsNotNull()) {
                double similarity = faceRecognitionService.calculateSimilarity(
                        inputEncoding,
                        guru.getFaceEncoding()
                );

                if (faceRecognitionService.isMatch(similarity)) {
                    // MATCH! Checkout via PresensiService
                    PresensiResponse response = presensiService.checkoutFace(guru);
                    return ResponseEntity.ok(response);
                }
            }

            // 4. Jika tidak ada yang match
            return ResponseEntity.status(404).body(
                    "Face tidak dikenali. Pastikan sudah enrollment dan foto clear."
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Checkout gagal: " + e.getMessage());
        }
    }
}
