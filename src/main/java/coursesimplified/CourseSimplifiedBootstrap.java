package coursesimplified;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import coursesimplified.api.CourseApiClient;
import coursesimplified.api.HttpCourseApiClient;
import coursesimplified.repository.ApiCourseRepository;
import coursesimplified.repository.CourseRepository;
import coursesimplified.service.CompletionService;
import coursesimplified.service.CourseTreeService;
import coursesimplified.service.JsonCompletionService;

import java.nio.file.Path;

public final class CourseSimplifiedBootstrap {
    private static final String API_BASE_URL = "https://course-api.gerardconsuelo.com";
    private static final Path COMPLETION_FILE = Path.of("completed.json");

    private CourseSimplifiedBootstrap() {
    }

    public static CourseTreeService createCourseTreeService() {
        Gson gson = new GsonBuilder().create();
        CourseApiClient apiClient = new HttpCourseApiClient(API_BASE_URL, gson);
        CourseRepository repository = new ApiCourseRepository(apiClient);
        CompletionService completionService = new JsonCompletionService(COMPLETION_FILE, gson);
        return new CourseTreeService(repository, completionService);
    }
}
