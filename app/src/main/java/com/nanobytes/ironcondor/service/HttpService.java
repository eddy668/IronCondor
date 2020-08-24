package com.nanobytes.ironcondor.service;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpService {
    private static HttpService instance;
    private static OkHttpClient client;
    private static Integer requests_in_queue;

    private HttpService() {
        requests_in_queue = 0;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        client = builder.build();
    }

    public static void send_get_request(String url, final Callback callback) {
        if (instance == null) instantiate_service();

        Request request = new Request.Builder().url(url).build();
        add_to_request_queue(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
                requests_in_queue--;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
                requests_in_queue--;
            }
        });
        requests_in_queue++;
    }

    static int number_of_requests_still_waiting() {
        return requests_in_queue;
    }

    public static void wait_for_all_requests() {
        if (instance == null) instantiate_service();

        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while(there_are_still_requests_waiting() && System.nanoTime() < end)
            Log.d("HttpService", "Waiting for all requests");
    }

    private static boolean there_are_still_requests_waiting() {
        return requests_in_queue > 0;
    }

    private static void instantiate_service() {
        instance = new HttpService();
    }

    private static void add_to_request_queue(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    public interface DoneWithHttpRequest {
        void done();
    }

}