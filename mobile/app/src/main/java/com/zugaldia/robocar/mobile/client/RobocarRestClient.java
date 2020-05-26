package com.zugaldia.robocar.mobile.client;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zugaldia.robocar.software.webserver.RobocarService;
import com.zugaldia.robocar.software.webserver.models.RobocarResponse;
import com.zugaldia.robocar.software.webserver.models.RobocarSpeed;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RobocarRestClient implements RobocarClient {

    RobocarService mRobocarService;

    public RobocarRestClient(String baseUrl) {
            mRobocarService = retrofit(baseUrl).create(RobocarService.class);
    }

    @Override
    public void setSpeed(Integer leftSpeed, Integer rightSpeed) {

        RobocarSpeed speed = new RobocarSpeed ();
        speed.setLeft(leftSpeed);
        speed.setRight(rightSpeed);

        Call<RobocarResponse> call = this.mRobocarService.postSpeed(speed);
        call.enqueue(new Callback<RobocarResponse>() {
            @Override
            public void onResponse(Call<RobocarResponse> call, Response<RobocarResponse> response) {
                Log.d("RRC.setSpeed", response!=null?response.message():"");
            }

            @Override
            public void onFailure(Call<RobocarResponse> call, Throwable t) {
                Log.d("RRC.setSpeed", t.getMessage());
            }
        });
    }

    @Override
    public void toggleRecord() {
        Call<RobocarResponse> call = this.mRobocarService.postRecord();
        call.enqueue(new Callback<RobocarResponse>() {
            @Override
            public void onResponse(Call<RobocarResponse> call, Response<RobocarResponse> response) {
                Log.d("RRC.record", response!=null?response.message():"");
            }

            @Override
            public void onFailure(Call<RobocarResponse> call, Throwable t) {
                Log.d("RRC.record", t.getMessage());
            }
        });

    }

    @Override
    public void toggleDrive() {
        Call<RobocarResponse> call = this.mRobocarService.postDrive();
        call.enqueue(new Callback<RobocarResponse>() {
            @Override
            public void onResponse(Call<RobocarResponse> call, Response<RobocarResponse> response) {
                Log.d("RRC.drive", response!=null?response.message():"");
            }

            @Override
            public void onFailure(Call<RobocarResponse> call, Throwable t) {
                Log.d("RRC.drive", t.getMessage());
            }
        });
    }

    private Retrofit retrofit(String baseUrl) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit;
    }
}
