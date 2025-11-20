package com.smk.presensi.mobile.api;

import com.smk.presensi.mobile.model.CheckinRequest;
import com.smk.presensi.mobile.model.LoginRequest;
import com.smk.presensi.mobile.model.LoginResponse;
import com.smk.presensi.mobile.model.PresensiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("presensi/checkin")
    Call<PresensiResponse> checkin(@Header("Authorization") String token, @Body CheckinRequest request);

    @GET("presensi/histori")
    Call<List<PresensiResponse>> getHistori(@Header("Authorization") String token);
}
