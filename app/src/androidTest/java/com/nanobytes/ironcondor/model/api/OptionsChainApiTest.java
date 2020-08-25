package com.nanobytes.ironcondor.model.api;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.nanobytes.ironcondor.RetryTest;
import com.nanobytes.ironcondor.model.OptionsChain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class OptionsChainApiTest extends UsesMockInputData{

    @Rule
    public RetryTest retry = new RetryTest(3);

    @Test
    public void can_pull_metrics_from_mocked_server() throws IOException {
        MockWebServer server = new MockWebServer();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream mock_json = this.getClass().getClassLoader().getResourceAsStream("options_chain.json");;

        String message_body = open_input_file(mock_json);
        server.enqueue(new MockResponse().setBody(message_body));
        server.start();

        OptionsChain chain = new OptionsChain("amd");
        OptionsChainApi options_api = new OptionsChainApi(server.url("").toString());
        options_api.get_options_chain(chain);

        options_api.wait_until_done();
        assertEquals("AMD", chain.ticker);
        assertEquals((Double) 83.8, chain.price);
        assertEquals(4, chain.strike_prices.size());
        assertEquals((Double) 82.0, chain.strike_prices.get(0));
        assertEquals((Double) 83.0, chain.strike_prices.get(1));
        assertEquals((Double) 84.0, chain.strike_prices.get(2));
        assertEquals((Double) 85.0, chain.strike_prices.get(3));
        assertEquals(4, chain.call_values.size());
        assertEquals((Double) 3.13, chain.call_values.get(0));
        assertEquals((Double) 2.52, chain.call_values.get(1));
        assertEquals((Double) 2.03, chain.call_values.get(2));
        assertEquals((Double) 1.58, chain.call_values.get(3));
        assertEquals(4, chain.put_values.size());
        assertEquals((Double) 1.31, chain.put_values.get(0));
        assertEquals((Double) 1.69, chain.put_values.get(1));
        assertEquals((Double) 2.19, chain.put_values.get(2));
        assertEquals((Double) 2.76, chain.put_values.get(3));
    }

    @Test
    public void will_pull_chain_from_unmocked_server() {
        OptionsChain chain = new OptionsChain("amd");

        OptionsChainApi options_api = new OptionsChainApi();
        options_api.get_options_chain(chain);

        options_api.wait_until_done();
        assertEquals("AMD", chain.ticker);
        assertTrue("Was not able to pull stock price", chain.price > 1);
        assertTrue("Was not able to pull strike prices", chain.strike_prices.size() > 0);
        assertTrue("Could not get call chain", chain.call_values.size() > 0);
        assertTrue("Could not get put chain", chain.put_values.size() > 0);
        assertTrue("The options chains are not the same size", chain.call_values.size() == chain.put_values.size());
    }
}
