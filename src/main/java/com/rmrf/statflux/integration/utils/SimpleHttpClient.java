package com.rmrf.statflux.integration.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Утилитарный класс для работы с API. Предоставляет методы инициации GET-запроса (в дальнейшем
 * класс можно расширить)
 */
@Slf4j
public class SimpleHttpClient {

    public static final Map<String, String> JSON_HEADERS = Map.of("Accept", "application/json",
        "Content-Type", "application/json");
    public static final int SUCCESS_GET_CODE = 200;

    private final int timeoutSeconds;
    private final HttpClient httpClient;

    public SimpleHttpClient() {
        this(15);
    }

    public SimpleHttpClient(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(timeoutSeconds)).build();
    }

    public String get(@NonNull String url) throws IOException, InterruptedException {
        return get(url, JSON_HEADERS);
    }

    public String get(@NonNull String url, @NonNull Map<String, String> headers)
        throws IOException, InterruptedException {
        HttpRequest request = buildRequest(url, headers);
        log.debug("GET: {}", url);

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        return handleResponse(response);
    }

    private HttpRequest buildRequest(@NonNull String url, @NonNull Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSeconds)).GET();

        headers.forEach(builder::header);

        return builder.build();
    }

    private String handleResponse(HttpResponse<String> response) {
        int code = response.statusCode();
        String body = response.body();

        if (code != SUCCESS_GET_CODE) {
            String error = String.format("HTTP %d: %s", code, body);
            log.error("Response error: {}", error);
            throw new RuntimeException(error);
        }

        return body;
    }
}