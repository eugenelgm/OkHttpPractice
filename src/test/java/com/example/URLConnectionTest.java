package com.example;

import com.squareup.okhttp.OkHttpConnection;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

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

    /**
     * Reads {@code count} characters from the stream. If the stream is
     * exhausted before {@code count} characters can be read, the remaining
     * characters are returned and the stream is closed.
     */
    private String readAscii(InputStream in, int count) throws IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int value = in.read();
            if (value == -1) {
                in.close();
                break;
            }
            result.append((char) value);
        }
        return result.toString();
    }

    /**
     * Reads at most {@code limit} characters from {@code in} and asserts that
     * content equals {@code expected}.
     */
    private void assertContent(String expected, URLConnection connection, int limit)
            throws IOException {
        connection.connect();
        assertEquals(expected, readAscii(connection.getInputStream(), limit));
        ((OkHttpConnection) connection).disconnect();
    }

    private void assertContent(String expected, URLConnection connection) throws IOException {
        assertContent(expected, connection, Integer.MAX_VALUE);
    }

    private void assertContains(Headers headers, String header) {
        assertTrue(/*headers.toString(), */headers.get(header) != null);
    }

    private void assertContainsNoneMatching(List<String> headers, String pattern) {
        for (String header : headers) {
            if (header.matches(pattern)) {
                fail("Header " + header + " matches " + pattern);
            }
        }
    }

    private Set<String> newSet(String... elements) {
        return new HashSet<String>(Arrays.asList(elements));
    }

    private static OkHttpConnection openConnection(HttpUrl url) {
        return OkHttpConnection.open(url.url());
    }

    @Test
    public void testRequestHeaders() throws IOException, InterruptedException {
        server.enqueue(new MockResponse());

        OkHttpConnection urlConnection = openConnection(server.url("/"));
        urlConnection.addRequestProperty("D", "e");
        urlConnection.addRequestProperty("D", "f");
        assertEquals("f", urlConnection.getRequestProperty("D"));
        assertEquals("f", urlConnection.getRequestProperty("d"));
        Map<String, List<String>> requestHeaders = urlConnection.getRequestProperties();
        assertEquals(newSet("e", "f"), new HashSet<String>(requestHeaders.get("D")));
        assertEquals(newSet("e", "f"), new HashSet<String>(requestHeaders.get("d")));
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

        int code = urlConnection.getResponseCode();
        assertTrue(code == 200);
        RecordedRequest request = server.takeRequest();
        assertContains(request.getHeaders(), "D");

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
