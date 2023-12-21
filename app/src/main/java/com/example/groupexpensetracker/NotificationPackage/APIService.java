package com.example.groupexpensetracker.NotificationPackage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=GET_YOUR_OWN"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}
