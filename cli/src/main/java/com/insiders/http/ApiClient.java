package com.insiders.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.insiders.dto.ResponseApi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
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
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setTimeZone(java.util.TimeZone.getTimeZone(ZoneId.of("Europe/Bucharest")));
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

    public <T> ApiResult<T> postMultipart(String path, Map<String, Object> formData, TypeReference<ResponseApi<T>> type) {
        try {
            String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");


            java.io.ByteArrayOutputStream bodyStream = new java.io.ByteArrayOutputStream();

            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                if (value == null) continue;


                bodyStream.write(("--" + boundary + "\r\n").getBytes("UTF-8"));

                if (value instanceof File) {
                    File file = (File) value;
                    if (file.exists() && file.isFile()) {
                        bodyStream.write(("Content-Disposition: form-data; name=\"" + name +
                                        "\"; filename=\"" + file.getName() + "\"\r\n").getBytes("UTF-8"));


                        String contentType = getContentType(file);
                        bodyStream.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes("UTF-8"));

                        try {
                            byte[] fileBytes = Files.readAllBytes(file.toPath());
                            bodyStream.write(fileBytes);
                        } catch (IOException e) {
                            return ApiResult.fail("Error reading file: " + e.getMessage(), null, 0);
                        }
                        bodyStream.write("\r\n".getBytes("UTF-8"));
                    }
                } else {
                    bodyStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes("UTF-8"));
                    bodyStream.write(value.toString().getBytes("UTF-8"));
                    bodyStream.write("\r\n".getBytes("UTF-8"));
                }
            }

            
            bodyStream.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));

            byte[] bodyBytes = bodyStream.toByteArray();

            HttpRequest req = base(path)
                    .header("Accept", "application/json")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
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
        } catch (Exception e){
            return ApiResult.fail("Connection/serialization error: " + e.getMessage(), null, 0);
        }
    }

    private String getContentType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (fileName.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
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
