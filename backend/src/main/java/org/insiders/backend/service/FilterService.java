package org.insiders.backend.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class FilterService {

    public static byte[] filterImage(byte[] imageBytes, String originalFilename, String filterName) {
//        try {
//            // Build the JSON array of operations
//            String operations = "[{ \"Type\": \"" + filterName + "\", \"Parameters\": {} }]";
//            String boundary = "----JavaBoundary-" + UUID.randomUUID();
//
//            // Build multipart/form-data body
//            var sb = new StringBuilder();
//            sb.append("--").append(boundary).append("\r\n");
//            sb.append("Content-Disposition: form-data; name=\"Operations\"\r\n\r\n");
//            sb.append(operations).append("\r\n");
//
//            // File header
//            sb.append("--").append(boundary).append("\r\n");
//            sb.append("Content-Disposition: form-data; name=\"Image\"; filename=\"")
//                    .append(originalFilename).append("\"\r\n");
//            sb.append("Content-Type: image/jpeg\r\n\r\n");
//
//            byte[] headerBytes = sb.toString().getBytes();
//            byte[] endBytes = ("\r\n--" + boundary + "--\r\n").getBytes();
//
//            // concatenate => header + image + ending
//            byte[] body = new byte[headerBytes.length + imageBytes.length + endBytes.length];
//            System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
//            System.arraycopy(imageBytes, 0, body, headerBytes.length, imageBytes.length);
//            System.arraycopy(endBytes, 0, body, headerBytes.length + imageBytes.length, endBytes.length);
//
//            // Send request to C# EC2
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(new URI("http://16.170.234.239/api/"))
//                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
//                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
//                    .build();
//
//            HttpClient client = HttpClient.newHttpClient();
//            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
//            if (response.statusCode() >= 200 && response.statusCode() < 300) {
//                return response.body(); // Response is binary JPEG
//            } else {
//                throw new RuntimeException("Filtering server returned status: " + response.statusCode());
//            }
//        } catch (IOException | InterruptedException | URISyntaxException e) {
//            throw new RuntimeException("Failed to process image through C# server", e);
//        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            String operationsJson = "[{ \"Type\": \"" + filterName + "\", \"Parameters\": {} }]";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("Image", new ByteArrayResource(imageBytes) {
                @Override public String getFilename() { return originalFilename; }
            });
            body.add("Operations", operationsJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response = restTemplate.postForEntity("http://16.170.234.239/api/", request, byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // JPEG bytes from C# server
            } else {
                throw new RuntimeException("C# server returned status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send image to C# server", e);
        }
    }
}
