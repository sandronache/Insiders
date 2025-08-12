package com.insiders.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.insiders.dto.ResponseApi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

public class ApiClient {
    private final String base;
    private final HttpClient http;
    private final ObjectMapper om;
    private final Supplier<Map<String,String>> headers;

    public ApiClient(String baseUrl, ObjectMapper mapper, Supplier<Map<String,String>> headers) {
        this.base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.om = (mapper != null ? mapper : new ObjectMapper())
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.headers = headers;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public <B,T> ApiResult<T> post(String path, B body, TypeReference<ResponseApi<T>> type){
        try {
            String json = om.writeValueAsString(body);
            HttpRequest req = base(path)
                    .header("Accept", "application/json")
                    .header("Content-Type","application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            int sc = resp.statusCode();
            String raw = resp.body();

            if (sc == 204) return ApiResult.ok(null, "No content", sc);
            if (raw == null || raw.isBlank()) {
                return sc >= 200 && sc < 300
                        ? ApiResult.ok(null, "OK (empty body)", sc)
                        : ApiResult.fail("Empty response body", null, sc);
            }

            try {
                ResponseApi<T> env = om.readValue(raw, type);
                String msg = (env.message() != null && !env.message().isBlank()) ? env.message() : "OK";
                if (sc >= 200 && sc < 300 && env.success()) return ApiResult.ok(env.data(), msg, sc);
                return ApiResult.fail(msg, env.data(), sc);
            } catch (Exception parseEx) {
                // fallback cand backend-ul nu a trimis JSON conform
                return ApiResult.fail(raw, null, sc);
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return ApiResult.fail("Request interrupted", null, 0);
        } catch (Exception e){
            return ApiResult.fail("Connection/serialization error: " + e.getMessage(), null, 0);
        }
    }

    public <T> ApiResult<T> get(String path, TypeReference<ResponseApi<T>> type) {
        try {
            HttpRequest req = base(path)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            int sc = resp.statusCode();
            String raw = resp.body();

            if (sc == 204) return ApiResult.ok(null, "No content", sc);
            if (raw == null || raw.isBlank()) {
                return sc >= 200 && sc < 300
                        ? ApiResult.ok(null, "OK (empty body)", sc)
                        : ApiResult.fail("Empty response body", null, sc);
            }

            try {
                ResponseApi<T> env = om.readValue(raw, type);
                String msg = (env.message() != null && !env.message().isBlank()) ? env.message() : "OK";
                if (sc >= 200 && sc < 300 && env.success()) return ApiResult.ok(env.data(), msg, sc);
                return ApiResult.fail(msg, env.data(), sc);
            } catch (Exception parseEx) {
                return ApiResult.fail(raw, null, sc);
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return ApiResult.fail("Request interrupted", null, 0);
        } catch (Exception e) {
            return ApiResult.fail("Connection/serialization error: " + e.getMessage(), null, 0);
        }
    }

    public <B, T> ApiResult<T> put(String path, B body, TypeReference<ResponseApi<T>> type) {
        try {
            String json = om.writeValueAsString(body);
            HttpRequest req = base(path)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            int sc = resp.statusCode();
            String raw = resp.body();

            if (sc == 204) return ApiResult.ok(null, "No content", sc);
            if (raw == null || raw.isBlank()) {
                return sc >= 200 && sc < 300
                        ? ApiResult.ok(null, "OK (empty body)", sc)
                        : ApiResult.fail("Empty response body", null, sc);
            }

            try {
                ResponseApi<T> env = om.readValue(raw, type);
                String msg = (env.message() != null && !env.message().isBlank()) ? env.message() : "OK";
                if (sc >= 200 && sc < 300 && env.success()) return ApiResult.ok(env.data(), msg, sc);
                return ApiResult.fail(msg, env.data(), sc);
            } catch (Exception parseEx) {
                return ApiResult.fail(raw, null, sc);
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return ApiResult.fail("Request interrupted", null, 0);
        } catch (Exception e) {
            return ApiResult.fail("Connection/serialization error: " + e.getMessage(), null, 0);
        }
    }

    public <T> ApiResult<T> delete(String path, TypeReference<ResponseApi<T>> type) {
        try {
            HttpRequest req = base(path)
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            int sc = resp.statusCode();
            String raw = resp.body();

            if (sc == 204) return ApiResult.ok(null, "No content", sc);
            if (raw == null || raw.isBlank()) {
                return sc >= 200 && sc < 300
                        ? ApiResult.ok(null, "OK (empty body)", sc)
                        : ApiResult.fail("Empty response body", null, sc);
            }

            try {
                ResponseApi<T> env = om.readValue(raw, type);
                String msg = (env.message() != null && !env.message().isBlank()) ? env.message() : "OK";
                if (sc >= 200 && sc < 300 && env.success()) return ApiResult.ok(env.data(), msg, sc);
                return ApiResult.fail(msg, env.data(), sc);
            } catch (Exception parseEx) {
                return ApiResult.fail(raw, null, sc);
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return ApiResult.fail("Request interrupted", null, 0);
        } catch (Exception e) {
            return ApiResult.fail("Connection/serialization error: " + e.getMessage(), null, 0);
        }
    }


    private HttpRequest.Builder base(String path){
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(base + (path.startsWith("/")?path:"/"+path)))
                .timeout(Duration.ofSeconds(10));
        if (headers != null) {
            headers.get().forEach(b::header);
        }
        return b;
    }
}
