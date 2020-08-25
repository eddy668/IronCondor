package com.nanobytes.ironcondor.model.api;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.nanobytes.ironcondor.RetryTest;
import com.nanobytes.ironcondor.model.OptionsChain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PricehistoryApiTest extends UsesMockInputData {
    @Rule
    public RetryTest retry = new RetryTest(3);

    @Test
    public void can_pull_metrics_from_mocked_server() throws IOException {
        MockWebServer server = new MockWebServer();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream mock_json = this.getClass().getClassLoader().getResourceAsStream("price_history.json");;

        String message_body = open_input_file(mock_json);
        server.enqueue(new MockResponse().setBody(message_body));
        server.start();

        LineGraphSeries<DataPoint> points = new LineGraphSeries<>();
        PriceHistoryApi price_history_api = new PriceHistoryApi(server.url("").toString());
        price_history_api.get_price_history("amd", points);

        price_history_api.wait_until_done();
        assertEquals(false, points.isEmpty());
        assertEquals(79.4, points.getLowestValueY(), 0.001);
        assertEquals(84.17, points.getHighestValueY(), 0.01);
    }

    @Test
    public void will_pull_chain_from_unmocked_server() {
        LineGraphSeries<DataPoint> points = new LineGraphSeries<>();
        PriceHistoryApi price_history_api = new PriceHistoryApi();
        price_history_api.get_price_history("amd", points);

        price_history_api.wait_until_done();
        assertEquals(false, points.isEmpty());
        assertTrue(points.getLowestValueY() > 50);
        assertTrue(points.getHighestValueY() > points.getLowestValueY());
    }
}
