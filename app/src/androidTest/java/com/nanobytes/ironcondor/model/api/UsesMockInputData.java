package com.nanobytes.ironcondor.model.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UsesMockInputData {
    public static String open_input_file(final InputStream input_stream) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input_stream));

        final StringBuilder stringBuilder = new StringBuilder();
        boolean done = false;
        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) stringBuilder.append(line);
        }

        reader.close();
        input_stream.close();

        return stringBuilder.toString();
    }
}
