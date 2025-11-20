package com.smk.presensi.controller;

import com.smk.presensi.dto.CreateQuizQuestionRequest;
import com.smk.presensi.dto.CreateQuizSessionRequest;
import com.smk.presensi.entity.QuizQuestion;
import com.smk.presensi.entity.QuizSession;
import com.smk.presensi.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<QuizSession> createSession(@RequestBody CreateQuizSessionRequest req) {
        QuizSession s = quizService.createSession(req);
        return ResponseEntity.created(URI.create("/api/quiz/sessions/" + s.getId())).body(s);
    }

    @GetMapping("/sessions")
    public java.util.List<QuizSession> listSessions() {
        return quizService.findAll();
    }

    @PostMapping("/sessions/{id}/questions")
    public ResponseEntity<QuizQuestion> addQuestion(@PathVariable("id") Long sessionId, @RequestBody CreateQuizQuestionRequest req) {
        QuizQuestion q = quizService.addQuestion(sessionId, req);
        return ResponseEntity.created(URI.create("/api/quiz/sessions/" + sessionId + "/questions/" + q.getId())).body(q);
    }

    @PostMapping("/sessions/{id}/token")
    public ResponseEntity<QuizSession> generateToken(@PathVariable("id") Long sessionId) {
        QuizSession s = quizService.generateToken(sessionId);
        return ResponseEntity.ok(s);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<QuizSession> getSession(@PathVariable Long id) {
        return quizService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
