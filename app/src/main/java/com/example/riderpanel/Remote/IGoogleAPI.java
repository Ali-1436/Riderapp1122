package com.example.riderpanel.Remote;

import retrofit2.http.GET;
import retrofit2.http.Query;


import io.reactivex.Observable;

public interface IGoogleAPI {
    //remmeber you must enable bolling for your project    to use this API
    @GET("maps/api/direction/json")
    Observable<String> getDirection(
            @Query("mode") String mode,
            @Query("transit_routing_preferences") String transit_routing ,
            @Query("origin") String from,
            @Query("destination") String to,
            @Query("key") String key
    );

}
