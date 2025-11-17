package com.smk.presensi.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * FACE RECOGNITION SERVICE (SIMPLIFIED VERSION).
 * 
 * Ini adalah SIMULASI face recognition untuk tujuan pembelajaran.
 * BUKAN real face recognition (tidak detect fitur wajah).
 * 
 * Untuk production, gunakan:
 * - face_recognition library (Python) + REST bridge
 * - AWS Rekognition / Azure Face API
 * - OpenCV + DNN model (C++/Python)
 * 
 * ═════════════════════════════════════════════════════════════
 * CARA KERJA SIMULASI
 * ═════════════════════════════════════════════════════════════
 * 
 * 1. ENROLLMENT:
 *    - Terima image base64
 *    - Decode ke byte array
 *    - Generate SHA-256 hash (64 hex chars)
 *    - Ambil first 128 chars (simulasi 128D face vector)
 *    - Simpan sebagai "face_encoding"
 * 
 * 2. RECOGNITION:
 *    - Terima image base64
 *    - Generate encoding (sama seperti enrollment)
 *    - Compare dengan semua enrolled encodings
 *    - Calculate similarity (Levenshtein distance)
 *    - If similarity > threshold (60%) → MATCH!
 * 
 * ═════════════════════════════════════════════════════════════
 * LIMITASI (untuk pembelajaran):
 * ═════════════════════════════════════════════════════════════
 * 
 * ❌ Tidak detect fitur wajah (mata, hidung, mulut)
 * ❌ Tidak robust terhadap pose/lighting changes
 * ❌ Tidak ada liveness detection (bisa pakai foto)
 * ❌ Similarity hanya berdasarkan image byte identity
 * 
 * Tapi MENGAJARKAN konsep:
 * ✅ Enrollment phase (register)
 * ✅ Recognition phase (match)
 * ✅ Threshold decision (kapan dianggap "sama")
 * ✅ Base64 image handling
 * ✅ Database storage untuk biometric data
 * 
 * @author SMK Teaching Team
 */
@Service
public class FaceRecognitionService {

    /**
     * Threshold untuk matching (0.0 - 1.0).
     * 
     * 1.0 = exact match (100% sama)
     * 0.6 = 60% similarity (recommended)
     * 0.0 = semua dianggap match
     * 
     * Untuk real face recognition:
     * - Threshold biasanya 0.6 - 0.7
     * - Terlalu tinggi (0.9): reject orang yang sama (false negative)
     * - Terlalu rendah (0.3): accept orang berbeda (false positive)
     */
    private static final double SIMILARITY_THRESHOLD = 0.6;

    /**
     * Panjang encoding (characters).
     * 
     * Real face recognition:
     * - FaceNet: 128D vector → 128 floats
     * - VGGFace: 512D vector → 512 floats
     * 
     * Kita simulasi: 128 characters dari SHA-256 hash.
     */
    private static final int ENCODING_LENGTH = 128;

    /**
     * GENERATE FACE ENCODING dari image base64.
     * 
     * Ini SIMULASI - real face recognition pakai CNN model.
     * 
     * Steps:
     * 1. Remove data URL prefix ("data:image/jpeg;base64,")
     * 2. Decode base64 → byte array
     * 3. Generate SHA-256 hash
     * 4. Convert hash ke hex string
     * 5. Duplicate sampai 128 chars (simulasi 128D vector)
     * 
     * @param imageBase64 Image dalam format base64 (with/without data URL prefix)
     * @return Face encoding (128 characters hex string)
     * @throws IllegalArgumentException jika image invalid
     */
    public String generateFaceEncoding(String imageBase64) {
        try {
            // 1. Clean base64 (remove data URL prefix if exist)
            String cleanBase64 = cleanBase64String(imageBase64);
            
            // 2. Decode base64 → byte array
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            
            // 3. Validate image size (minimal 1KB, maksimal 10MB)
            validateImageSize(imageBytes);
            
            // 4. Generate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(imageBytes);
            
            // 5. Convert hash ke hex string (64 characters)
            String hexHash = bytesToHex(hashBytes);
            
            // 6. Duplicate untuk dapat 128 chars (simulasi 128D vector)
            String encoding = (hexHash + hexHash).substring(0, ENCODING_LENGTH);
            
            return encoding;
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 image: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available");
        }
    }

    /**
     * CALCULATE SIMILARITY antara dua face encodings.
     * 
     * Real face recognition pakai:
     * - Cosine similarity (recommended)
     * - Euclidean distance
     * - Manhattan distance
     * 
     * Kita simulasi pakai string similarity (Levenshtein distance).
     * 
     * Formula:
     * similarity = 1.0 - (levenshtein_distance / max_length)
     * 
     * Examples:
     * - "abc" vs "abc" → distance=0 → similarity=1.0 (100%)
     * - "abc" vs "xyz" → distance=3 → similarity=0.0 (0%)
     * - "abc" vs "adc" → distance=1 → similarity=0.67 (67%)
     * 
     * @param encoding1 Face encoding 1
     * @param encoding2 Face encoding 2
     * @return Similarity score (0.0 - 1.0)
     */
    public double calculateSimilarity(String encoding1, String encoding2) {
        if (encoding1 == null || encoding2 == null) {
            return 0.0;
        }
        
        // Levenshtein distance (edit distance)
        int distance = levenshteinDistance(encoding1, encoding2);
        
        // Normalize ke 0.0 - 1.0
        int maxLength = Math.max(encoding1.length(), encoding2.length());
        double similarity = 1.0 - ((double) distance / maxLength);
        
        return similarity;
    }

    /**
     * CHECK apakah similarity memenuhi threshold.
     * 
     * @param similarity Similarity score (0.0 - 1.0)
     * @return true jika similarity >= threshold
     */
    public boolean isMatch(double similarity) {
        return similarity >= SIMILARITY_THRESHOLD;
    }

    /**
     * GET threshold value (for debugging/logging).
     * 
     * @return Current similarity threshold
     */
    public double getThreshold() {
        return SIMILARITY_THRESHOLD;
    }

    // ═════════════════════════════════════════════════════════════
    // PRIVATE HELPER METHODS
    // ═════════════════════════════════════════════════════════════

    /**
     * Clean base64 string (remove data URL prefix).
     * 
     * Input bisa dalam format:
     * - "data:image/jpeg;base64,/9j/4AAQ..."  (dengan prefix)
     * - "/9j/4AAQ..."  (tanpa prefix)
     * 
     * Output: base64 string murni (tanpa prefix).
     * 
     * @param base64 Base64 string (with or without prefix)
     * @return Clean base64 string
     */
    private String cleanBase64String(String base64) {
        if (base64 == null || base64.isEmpty()) {
            throw new IllegalArgumentException("Base64 string cannot be empty");
        }
        
        // Remove data URL prefix if exists
        if (base64.contains(",")) {
            return base64.substring(base64.indexOf(",") + 1);
        }
        
        return base64;
    }

    /**
     * Validate image size.
     * 
     * Rules:
     * - Minimal: 1 KB (1024 bytes)
     * - Maksimal: 10 MB (10 * 1024 * 1024 bytes)
     * 
     * @param imageBytes Image byte array
     * @throws IllegalArgumentException jika size invalid
     */
    private void validateImageSize(byte[] imageBytes) {
        int size = imageBytes.length;
        
        if (size < 1024) {
            throw new IllegalArgumentException(
                "Image too small: " + size + " bytes (min: 1 KB)"
            );
        }
        
        if (size > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Image too large: " + size + " bytes (max: 10 MB)"
            );
        }
    }

    /**
     * Convert byte array ke hex string.
     * 
     * Example:
     * [0x1A, 0x2B, 0x3C] → "1a2b3c"
     * 
     * @param bytes Byte array
     * @return Hex string (lowercase)
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Calculate Levenshtein distance (edit distance).
     * 
     * Algoritma dynamic programming untuk hitung berapa edit
     * (insert, delete, replace) yang diperlukan untuk ubah
     * string1 jadi string2.
     * 
     * Example:
     * - "kitten" vs "sitting" → distance = 3
     *   (replace k→s, replace e→i, insert g)
     * 
     * Complexity: O(n*m) time, O(n*m) space
     * 
     * @param s1 String 1
     * @param s2 String 2
     * @return Edit distance
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        // DP table: dp[i][j] = distance from s1[0..i] to s2[0..j]
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // Base case: empty string
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;  // Delete all chars from s1
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;  // Insert all chars to s2
        }
        
        // Fill DP table
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    // Characters sama, no cost
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // Characters beda, ambil minimum dari:
                    // - Replace: dp[i-1][j-1] + 1
                    // - Delete:  dp[i-1][j] + 1
                    // - Insert:  dp[i][j-1] + 1
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),
                        dp[i - 1][j - 1]
                    ) + 1;
                }
            }
        }
        
        return dp[len1][len2];
    }
}
