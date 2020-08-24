package com.nanobytes.ironcondor.model.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.nanobytes.ironcondor.model.OptionsChain;
import com.nanobytes.ironcondor.service.HttpService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TdAmeritradeApi extends BaseHttpApi {
    private String api_url;

    public TdAmeritradeApi(String url) {
        this.api_url = url;
    }

    public TdAmeritradeApi() {
        this("https://api.tdameritrade.com/v1/marketdata/chains?apikey=EXJ0IKLVL062E92L9JEAZX4LNOGQBE7F&symbol=AMD&strikeCount=20&toDate="+get_next_weekly_expiration());
    }

    public void get_options_chain(final OptionsChain chain) {
        started_a_process();
        HttpService.send_get_request(api_url + chain.ticker, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                could_not_contact_tdameritrade(chain);
                finished_a_process();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                process_tdameritrade_json(response, chain);
                finished_a_process();
            }
        });
    }

    private void could_not_contact_tdameritrade(OptionsChain chain) {
        Log.e("TdAmeritradeApi", "Could not receive response from server.");
        reset_chain(chain);
    }

    private void process_tdameritrade_json(Response response, OptionsChain chain) {
        try {
            String response_body = Objects.requireNonNull(response.body()).string();
            parse_json_response(response_body, chain);
        } catch (IOException | JSONException e) {
            Log.e("CoinGeckoApi", "Could not parse JSON from CoinGecko", e);
            reset_chain(chain);
        }
    }

    private void parse_json_response(String response_body, OptionsChain chain) throws JSONException {
        JSONObject json_response = new JSONObject(response_body);
        JSONObject put_data = json_response.getJSONObject("putExpDateMap");
        JSONObject call_data = json_response.getJSONObject("callExpDateMap");

        chain.ticker = json_response.getString("symbol");
        chain.price = json_response.getDouble("underlyingPrice");
        chain.strike_prices = get_strike_prices_from_json(put_data);
        chain.call_values = get_option_values_from_json(call_data);
        chain.put_values = get_option_values_from_json(put_data);
    }

    private List<Double> get_strike_prices_from_json(JSONObject put_data) throws JSONException {
        ArrayList<Double> strike_prices = new ArrayList<>();
        String first_child = put_data.keys().next();

        JSONObject contracts = put_data.getJSONObject(first_child);
        Iterator<String> keys = contracts.keys();
        while (keys.hasNext())
            strike_prices.add( Double.parseDouble((String)keys.next()) );
        return strike_prices;
    }

    private List<Double> get_option_values_from_json(JSONObject call_data) throws JSONException {
        ArrayList<Double> values = new ArrayList<>();
        String first_child = call_data.keys().next();

        JSONObject contracts = call_data.getJSONObject(first_child);
        Iterator<String> keys = contracts.keys();
        while (keys.hasNext()) {
            Double mark = contracts.getJSONArray(keys.next()).getJSONObject(0).getDouble("mark");
            values.add(mark);
        }
        return values;
    }

    private void reset_chain(OptionsChain chain) {
        chain.ticker = "";
        chain.price = 0.0;
    }

    private static String get_next_weekly_expiration() {
        LocalDate next_friday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        return next_friday.toString();
    }
}
