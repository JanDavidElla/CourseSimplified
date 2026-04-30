package coursesimplified.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import coursesimplified.api.dto.CourseDetailDto;
import coursesimplified.api.dto.CourseTreeResponseDto;
import coursesimplified.api.dto.ProgramDto;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpCourseApiClient implements CourseApiClient {
    private final String baseUrl;
    private final Gson gson;
    private final HttpClient httpClient;

    public HttpCourseApiClient(String baseUrl, Gson gson) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.gson = gson;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public List<ProgramDto> fetchPrograms() {
        String body = get("/api/programs");
        JsonObject json = gson.fromJson(body, JsonObject.class);
        Type listType = new TypeToken<List<ProgramDto>>() {}.getType();
        return gson.fromJson(json.get("data"), listType);
    }

    @Override
    public CourseTreeResponseDto fetchCourseTree(String poid) {
        String body = get("/api/course_tree/" + poid);
        return gson.fromJson(body, CourseTreeResponseDto.class);
    }

    @Override
    public CourseDetailDto fetchCourseDetail(String courseCode) {
        String encoded = URLEncoder.encode(courseCode, StandardCharsets.UTF_8).replace("+", "%20");
        String body = get("/api/course/" + encoded);
        JsonObject json = gson.fromJson(body, JsonObject.class);
        return gson.fromJson(json.get("data"), CourseDetailDto.class);
    }

    private String get(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("API returned HTTP " + response.statusCode() + " for " + path, response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request failed for " + path, e);
        }
    }
}
