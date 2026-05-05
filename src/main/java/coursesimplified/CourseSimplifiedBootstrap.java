package coursesimplified;

import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import coursesimplified.api.CourseApiClient;
import coursesimplified.api.HttpCourseApiClient;
import coursesimplified.repository.ApiCourseRepository;
import coursesimplified.repository.CourseRepository;
import coursesimplified.service.CompletionService;
import coursesimplified.service.CourseTreeService;
import coursesimplified.service.JsonCompletionService;
import coursesimplified.service.UserService;

public final class CourseSimplifiedBootstrap {
    private static final String API_BASE_URL = "https://course-api.gerardconsuelo.com";
    private static final String DEFAULT_USER_ID = "cli";
    private static final Path USERS_FILE = Path.of("users.json");
    private static final Path COMPLETION_FILE = Path.of("completed.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private CourseSimplifiedBootstrap() {
    }

    public static UserService createUserService() {
        return new UserService(USERS_FILE, GSON);
    }

    public static CourseTreeService createCourseTreeService() {
        return createCourseTreeService(DEFAULT_USER_ID);
    }

    public static CourseTreeService createCourseTreeService(String userId) {
        CourseApiClient apiClient = new HttpCourseApiClient(API_BASE_URL, GSON);
        CourseRepository repository = new ApiCourseRepository(apiClient);
        CompletionService completionService = new JsonCompletionService(COMPLETION_FILE, GSON, userId);
        return new CourseTreeService(repository, completionService);
    }
}
