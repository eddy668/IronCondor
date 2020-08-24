package com.nanobytes.ironcondor.service;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nanobytes.ironcondor.RetryTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;

@RunWith(AndroidJUnit4.class)
public class HttpServiceTest {
    private String server_response = "";

    @Rule
    public RetryTest retry = new RetryTest(3);

    @Before
    public void set_up() throws NoSuchFieldException, IllegalAccessException {
        Field instance = HttpService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        server_response = "Waiting";
    }

    @Test
    public void  wait_for_requests_before_sending_request_should_initialize_service() {
        try {
            HttpService.wait_for_all_requests();
        } catch (NullPointerException npe) {
            fail("The HttpService should have initialized itself");
        }
    }

    @Test
    public void make_sure_requests_are_being_tracked() throws IOException {
        MockWebServer server = new MockWebServer();
        String message_body = "This Is A Message";
        server.enqueue(new MockResponse().setBody(message_body));
        server.enqueue(new MockResponse().setBody(message_body));
        server.enqueue(new MockResponse().setBody(message_body));
        server.start();

        HttpUrl base_url = server.url("");
        HttpService.send_get_request(base_url.toString(), new DummyCallback());
        assertEquals(1, HttpService.number_of_requests_still_waiting());

        HttpService.send_get_request(base_url.toString(), new DummyCallback());
        assertEquals(2, HttpService.number_of_requests_still_waiting());

        HttpService.send_get_request(base_url.toString(), new DummyCallback());
        assertEquals(3, HttpService.number_of_requests_still_waiting());

        HttpService.wait_for_all_requests();
        assertEquals(0, HttpService.number_of_requests_still_waiting());
    }

    @Test
    public void will_successfully_callback_valid_url() throws Exception {
        MockWebServer server = new MockWebServer();
        String message_body = "This Is A Message";
        server.enqueue(new MockResponse().setBody(message_body));
        server.start();

        HttpUrl base_url = server.url("");
        HttpService.send_get_request(base_url.toString(), new DummyCallback());
        assertEquals(1, HttpService.number_of_requests_still_waiting());

        HttpService.wait_for_all_requests();
        Thread.sleep(200);
        assertEquals(0, HttpService.number_of_requests_still_waiting());
        assertEquals(message_body, server_response);
    }

    @Test
    public void will_fail_on_invalid_url() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        server.close();

        HttpUrl base_url = server.url("");
        HttpService.send_get_request(base_url.toString(), new DummyCallback());
        assertEquals(1, HttpService.number_of_requests_still_waiting());

        HttpService.wait_for_all_requests();
        assertEquals(0, HttpService.number_of_requests_still_waiting());
        assertEquals("Failed", server_response);
    }

    class DummyCallback implements Callback {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.d("DummyCallback", "Callback Failed", e);
            server_response = "Failed";
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Log.d("DummyCallback", "Callback Succeeded");
            server_response = Objects.requireNonNull(response.body()).string();
        }
    }
}