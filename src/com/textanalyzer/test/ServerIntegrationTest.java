package com.textanalyzer.test;

import com.textanalyzer.server.TextAnalyzerServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Direct HTTP integration test verifying the Web Server endpoints.
 */
public class ServerIntegrationTest {

    public static void main(String[] args) {
        int testPort = 8099;
        try {
            TextAnalyzerServer server = new TextAnalyzerServer(testPort, "web");
            server.start();

            HttpClient client = HttpClient.newHttpClient();

            // 1. Test Static Index.html
            HttpRequest htmlReq = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + testPort + "/"))
                    .GET()
                    .build();
            HttpResponse<String> htmlResp = client.send(htmlReq, HttpResponse.BodyHandlers.ofString());
            System.out.println("Static HTML GET Status: " + htmlResp.statusCode());
            assert htmlResp.statusCode() == 200 : "Expected 200 OK for /";
            assert htmlResp.body().contains("LexiCore Analyzer") : "Expected HTML content";

            // 2. Test POST /api/analyze
            String jsonPayload = "{\"text\":\"Object-Oriented Programming in Java is powerful.\",\"sourceName\":\"IntegrationTest\"}";
            HttpRequest apiReq = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + testPort + "/api/analyze"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> apiResp = client.send(apiReq, HttpResponse.BodyHandlers.ofString());
            System.out.println("API /api/analyze Status: " + apiResp.statusCode());
            System.out.println("API Response: " + apiResp.body());
            assert apiResp.statusCode() == 200 : "Expected 200 OK for /api/analyze";
            assert apiResp.body().contains("\"success\":true") : "Expected success:true";

            server.stop();
            System.out.println("\n==> ALL SERVER INTEGRATION TESTS PASSED SUCCESSFULLY! <==");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
