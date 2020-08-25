package com.nanobytes.ironcondor.model.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.nanobytes.ironcondor.model.OptionsChain;
import com.nanobytes.ironcondor.service.HttpService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PriceHistoryApi extends BaseHttpApi  {
    private String api_url;

    public PriceHistoryApi(String url) {
        this.api_url = url;
    }

    public PriceHistoryApi() {
        this("https://api.tdameritrade.com//v1/marketdata/***/pricehistory?apikey=EXJ0IKLVL062E92L9JEAZX4LNOGQBE7F&periodType=day&period=10&frequencyType=minute&frequency=30");
    }

    public void get_price_history(String symbol, final LineGraphSeries<DataPoint> points) {
        started_a_process();
        HttpService.send_get_request(api_url.replace("***", symbol.toUpperCase()), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                could_not_contact_tdameritrade();
                finished_a_process();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                process_tdameritrade_json(response, points);
                finished_a_process();
            }
        });
    }

    private void could_not_contact_tdameritrade() {
        Log.e("PriceHistoryApi", "Could not receive response from server.");
    }

    private void process_tdameritrade_json(Response response, LineGraphSeries<DataPoint> points) {
        try {
            String response_body = Objects.requireNonNull(response.body()).string();
            parse_json_response(response_body, points);
        } catch (IOException | JSONException e) {
            Log.e("PriceHistoryApi", "Could not parse JSON from CoinGecko", e);
        }
    }

    private void parse_json_response(String response_body, LineGraphSeries<DataPoint> points) throws JSONException {
        JSONObject json_response = new JSONObject(response_body);
        JSONArray candles = json_response.getJSONArray("candles");

        for (int i=0; i < candles.length(); i++) {
            Double marker = candles.getJSONObject(i).getDouble("close");
            DataPoint point = new DataPoint(i, marker);
            points.appendData(point, false, 10000);
        }
    }
}
