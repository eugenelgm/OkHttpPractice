package com.example;

import com.squareup.okhttp.OkHttpConnection;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.ResponseCache;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class URLConnectionTest {

    private static final Authenticator SIMPLE_AUTHENTICATOR = new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("username", "password".toCharArray());
        }
    };

    /** base64("username:password") */
    private static final String BASE_64_CREDENTIALS = "dXNlcm5hbWU6cGFzc3dvcmQ=";

    private MockWebServer server = new MockWebServer();
    private String hostName;

    @BeforeAll
    public void setUp() throws Exception {
        hostName = server.getHostName();
    }

    @AfterAll
    public void tearDown() throws Exception {
        ResponseCache.setDefault(null);
        Authenticator.setDefault(null);
        System.clearProperty("proxyHost");
        System.clearProperty("proxyPort");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        server.shutdown();
    }

    private static OkHttpConnection openConnection(HttpUrl url) {
        return OkHttpConnection.open(url.url());
    }

    @Test
    public void testRequestHeaders() throws IOException, InterruptedException {
        server.enqueue(new MockResponse());
        //server.start();

        OkHttpConnection urlConnection = openConnection(server.url("/"));
        urlConnection.addRequestProperty("D", "e");
        urlConnection.addRequestProperty("D", "f");
        assertEquals("f", urlConnection.getRequestProperty("D"));
        assertEquals("f", urlConnection.getRequestProperty("d"));
        Map<String, List<String>> requestHeaders = urlConnection.getRequestProperties();
        //assertEquals(newSet("e", "f"), new HashSet<String>(requestHeaders.get("D")));
        //assertEquals(newSet("e", "f"), new HashSet<String>(requestHeaders.get("d")));
        try {
            requestHeaders.put("G", Arrays.asList("h"));
            fail("Modified an unmodifiable view.");
        } catch (UnsupportedOperationException expected) {
        }
        try {
            requestHeaders.get("D").add("i");
            fail("Modified an unmodifiable view.");
        } catch (UnsupportedOperationException expected) {
        }
        try {
            urlConnection.setRequestProperty(null, "j");
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            urlConnection.addRequestProperty(null, "k");
            fail();
        } catch (NullPointerException expected) {
        }
        urlConnection.setRequestProperty("NullValue", null); // should fail silently!
        assertNull(urlConnection.getRequestProperty("NullValue"));
        urlConnection.addRequestProperty("AnotherNullValue", null);  // should fail silently!
        assertNull(urlConnection.getRequestProperty("AnotherNullValue"));

        urlConnection.getResponseCode();
        RecordedRequest request = server.takeRequest();
//        assertContains(request.getHeaders(), "D: e");
//        assertContains(request.getHeaders(), "D: f");
//        assertContainsNoneMatching(request.getHeaders(), "NullValue.*");
//        assertContainsNoneMatching(request.getHeaders(), "AnotherNullValue.*");
//        assertContainsNoneMatching(request.getHeaders(), "G:.*");
//        assertContainsNoneMatching(request.getHeaders(), "null:.*");

        try {
            urlConnection.addRequestProperty("N", "o");
            fail("Set header after connect");
        } catch (IllegalStateException expected) {
        }
        try {
            urlConnection.setRequestProperty("P", "q");
            fail("Set header after connect");
        } catch (IllegalStateException expected) {
        }
        try {
            urlConnection.getRequestProperties();
            fail();
        } catch (IllegalStateException expected) {
        }
    }
}
