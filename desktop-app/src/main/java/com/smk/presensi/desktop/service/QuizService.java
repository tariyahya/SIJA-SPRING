package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.QuizQuestion;
import com.smk.presensi.desktop.model.QuizSession;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service untuk manajemen Quiz (session + question) di desktop app.
 */
public class QuizService {

    private final ApiClient apiClient;
    private final Gson gson;

    public QuizService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class,
                        (com.google.gson.JsonDeserializer<LocalDate>) (json, type, context) ->
                                LocalDate.parse(json.getAsString()))
                .create();
    }

    /**
     * Create new quiz session.
     */
    public QuizSession createSession(QuizSession session) throws IOException, InterruptedException {
        CreateSessionBody body = new CreateSessionBody();
        body.guruId = session.getGuruId();
        body.kelasId = session.getKelasId();
        body.judul = session.getJudul();
        body.mapel = session.getMapel();
        body.materi = session.getMateri();
        body.tanggal = session.getTanggal();

        String jsonBody = gson.toJson(body);
        HttpResponse<String> response = apiClient.post("/quiz/sessions", jsonBody);
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), QuizSession.class);
        }
        throw new IOException("Failed to create quiz session: " + response.statusCode() + " - " + response.body());
    }

    /**
     * Add question to existing session.
     */
    public QuizQuestion addQuestion(Long sessionId, QuizQuestion question) throws IOException, InterruptedException {
        CreateQuestionBody body = new CreateQuestionBody();
        body.soal = question.getSoal();
        body.opsiA = question.getOpsiA();
        body.opsiB = question.getOpsiB();
        body.opsiC = question.getOpsiC();
        body.opsiD = question.getOpsiD();
        body.jawabanBenar = question.getJawabanBenar();
        body.bobot = question.getBobot();

        String jsonBody = gson.toJson(body);
        HttpResponse<String> response = apiClient.post("/quiz/sessions/" + sessionId + "/questions", jsonBody);
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            QuizQuestion created = gson.fromJson(response.body(), QuizQuestion.class);
            created.setSessionId(sessionId);
            return created;
        }
        throw new IOException("Failed to add question: " + response.statusCode() + " - " + response.body());
    }

    /**
     * Generate token & QR URL for session.
     */
    public QuizSession generateToken(Long sessionId) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.post("/quiz/sessions/" + sessionId + "/token", "{}");
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), QuizSession.class);
        }
        throw new IOException("Failed to generate token: " + response.statusCode() + " - " + response.body());
    }

    /**
     * Get session by ID (detail, including questions).
     */
    public QuizSession getSession(Long sessionId) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/quiz/sessions/" + sessionId);
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), QuizSession.class);
        }
        if (response.statusCode() == 404) {
            return null;
        }
        throw new IOException("Failed to get session: " + response.statusCode() + " - " + response.body());
    }

    /**
     * List all quiz sessions (requires backend support).
     * Jika backend belum punya endpoint /api/quiz/sessions (GET),
     * method ini akan mengembalikan list kosong.
     */
    public List<QuizSession> listSessions() {
        try {
            HttpResponse<String> response = apiClient.get("/quiz/sessions");
            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<QuizSession>>(){}.getType();
                return gson.fromJson(response.body(), listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Helper untuk format tanggal ke string singkat.
     */
    public String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static class CreateSessionBody {
        Long guruId;
        String judul;
        Long kelasId;
        String mapel;
        String materi;
        LocalDate tanggal;
    }

    private static class CreateQuestionBody {
        String soal;
        String opsiA;
        String opsiB;
        String opsiC;
        String opsiD;
        String jawabanBenar;
        Integer bobot;
    }
}
