package com.smk.presensi.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smk.presensi.mobile.R;
import com.smk.presensi.mobile.api.ApiClient;
import com.smk.presensi.mobile.api.ApiService;
import com.smk.presensi.mobile.model.CheckinRequest;
import com.smk.presensi.mobile.model.PresensiResponse;
import com.smk.presensi.mobile.model.TipeUser;
import com.smk.presensi.mobile.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvHistory;
    private Button btnCheckin, btnRefresh, btnLogout;
    private ProgressBar progressBar;
    private String token;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        
        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvHistory = findViewById(R.id.tvHistory);
        btnCheckin = findViewById(R.id.btnCheckin);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        token = "Bearer " + sessionManager.getToken();
        String username = sessionManager.getUsername();

        tvWelcome.setText("Welcome, " + username);

        btnCheckin.setOnClickListener(v -> checkin());
        btnRefresh.setOnClickListener(v -> loadHistory());
        btnLogout.setOnClickListener(v -> logout());
        
        loadHistory();
    }

    private void checkin() {
        setLoading(true);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // Hardcoded location for demo (Jakarta)
        CheckinRequest request = new CheckinRequest(TipeUser.SISWA, -6.200000, 106.816666, "Mobile Checkin");
        
        Call<PresensiResponse> call = apiService.checkin(token, request);
        call.enqueue(new Callback<PresensiResponse>() {
            @Override
            public void onResponse(Call<PresensiResponse> call, Response<PresensiResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "Checkin berhasil!", Toast.LENGTH_SHORT).show();
                    loadHistory();
                } else {
                    Toast.makeText(DashboardActivity.this, "Checkin gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PresensiResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(DashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadHistory() {
        setLoading(true);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<PresensiResponse>> call = apiService.getHistori(token);

        call.enqueue(new Callback<List<PresensiResponse>>() {
            @Override
            public void onResponse(Call<List<PresensiResponse>> call, Response<List<PresensiResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PresensiResponse> list = response.body();
                    if (list.isEmpty()) {
                        tvHistory.setText("Belum ada data presensi");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (PresensiResponse p : list) {
                            sb.append("📅 ").append(p.getTanggal())
                              .append("\n⏰ ").append(p.getJamMasuk())
                              .append(" | Status: ").append(p.getStatus())
                              .append("\n\n");
                        }
                        tvHistory.setText(sb.toString());
                    }
                } else {
                    tvHistory.setText("Gagal memuat history");
                }
            }

            @Override
            public void onFailure(Call<List<PresensiResponse>> call, Throwable t) {
                setLoading(false);
                tvHistory.setText("Error: " + t.getMessage());
            }
        });
    }
    
    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnCheckin.setEnabled(!loading);
        btnRefresh.setEnabled(!loading);
    }
}
