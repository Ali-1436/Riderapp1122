package com.example.riderpanel.Remote;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RerofitClient {
    private static Retrofit Instance;

    public static Retrofit getInstance(){
        return Instance == null ? new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/") // dont forget last /
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build() : Instance;


    }

}
