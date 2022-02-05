package com.example;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class TestSample {

    @Test
    public void test1() {
        System.out.println("Test!");
    }

    @Test
    public void testUrlConnection() {
        URL url = null;
        try {
            url = new URL("http://www.android.com/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            StringBuffer buffer = readStream(urlConnection);
            System.out.println(buffer.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {

        }
    }

    private StringBuffer readStream(HttpURLConnection con) throws IOException {
        Charset charset = Charset.forName("UTF-8");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),charset));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response;
    }


}
