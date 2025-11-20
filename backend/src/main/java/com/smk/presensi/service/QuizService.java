package com.smk.presensi.service;

import com.smk.presensi.dto.CreateQuizQuestionRequest;
import com.smk.presensi.dto.CreateQuizSessionRequest;
import com.smk.presensi.entity.Guru;
import com.smk.presensi.entity.Kelas;
import com.smk.presensi.entity.QuizQuestion;
import com.smk.presensi.entity.QuizSession;
import com.smk.presensi.enums.QuizStatus;
import com.smk.presensi.repository.GuruRepository;
import com.smk.presensi.repository.KelasRepository;
import com.smk.presensi.repository.QuizQuestionRepository;
import com.smk.presensi.repository.QuizSessionRepository;
import com.smk.presensi.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class QuizService {

    private final QuizSessionRepository sessionRepo;
    private final QuizQuestionRepository questionRepo;
    private final GuruRepository guruRepository;
    private final KelasRepository kelasRepository;

    public QuizService(QuizSessionRepository sessionRepo,
                       QuizQuestionRepository questionRepo,
                       GuruRepository guruRepository,
                       KelasRepository kelasRepository) {
        this.sessionRepo = sessionRepo;
        this.questionRepo = questionRepo;
        this.guruRepository = guruRepository;
        this.kelasRepository = kelasRepository;
    }

    @Transactional
    public QuizSession createSession(CreateQuizSessionRequest req) {
        Guru guru = guruRepository.findById(req.getGuruId())
            .orElseThrow(() -> new RuntimeException("Guru not found: " + req.getGuruId()));
        Kelas kelas = kelasRepository.findById(req.getKelasId())
            .orElseThrow(() -> new RuntimeException("Kelas not found: " + req.getKelasId()));

        QuizSession s = new QuizSession();
        s.setGuru(guru);
        s.setJudul(req.getJudul());
        s.setKelas(kelas);
        s.setMapel(req.getMapel());
        s.setMateri(req.getMateri());
        s.setTanggal(req.getTanggal() != null ? req.getTanggal() : java.time.LocalDate.now());
        s.setStatus(QuizStatus.DRAFT);
        return sessionRepo.save(s);
    }

    @Transactional
    public QuizQuestion addQuestion(Long sessionId, CreateQuizQuestionRequest req) {
        QuizSession session = sessionRepo.findById(sessionId).orElseThrow(() -> new RuntimeException("QuizSession not found"));
        QuizQuestion q = new QuizQuestion();
        q.setSession(session);
        q.setSoal(req.getSoal());
        q.setOpsiA(req.getOpsiA());
        q.setOpsiB(req.getOpsiB());
        q.setOpsiC(req.getOpsiC());
        q.setOpsiD(req.getOpsiD());
        q.setJawabanBenar(req.getJawabanBenar());
        q.setBobot(req.getBobot() != null ? req.getBobot() : 1);
        return questionRepo.save(q);
    }

    @Transactional
    public QuizSession generateToken(Long sessionId) {
        QuizSession session = sessionRepo.findById(sessionId).orElseThrow(() -> new RuntimeException("QuizSession not found"));
        String token = TokenUtil.generateToken(8);
        session.setToken(token);
        // simple QR placeholder URL; desktop or frontend can render QR from this URL
        session.setQrCodeUrl("/api/quiz/sessions/" + sessionId + "/join?token=" + token);
        session.setStatus(QuizStatus.ACTIVE);
        return sessionRepo.save(session);
    }

    public Optional<QuizSession> findById(Long id) {
        return sessionRepo.findById(id);
    }

    public java.util.List<QuizSession> findAll() {
        return sessionRepo.findAll();
    }
}
