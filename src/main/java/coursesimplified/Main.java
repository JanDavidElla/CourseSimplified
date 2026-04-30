package coursesimplified;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import coursesimplified.api.CourseApiClient;
import coursesimplified.api.HttpCourseApiClient;
import coursesimplified.cli.CliRunner;
import coursesimplified.display.ConsoleTreeDisplay;
import coursesimplified.display.CourseTreeDisplay;
import coursesimplified.repository.ApiCourseRepository;
import coursesimplified.repository.CourseRepository;
import coursesimplified.service.CompletionService;
import coursesimplified.service.CourseTreeService;
import coursesimplified.service.FileCompletionService;

import java.nio.file.Path;

public class Main {
    private static final String API_BASE_URL = "https://course-api.gerardconsuelo.com";

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().create();
        CourseApiClient apiClient = new HttpCourseApiClient(API_BASE_URL, gson);
        CourseRepository repository = new ApiCourseRepository(apiClient);
        CompletionService completionService = new FileCompletionService(Path.of("completed.txt"));
        CourseTreeService service = new CourseTreeService(repository, completionService);
        CourseTreeDisplay display = new ConsoleTreeDisplay();
        new CliRunner(service, display).run();
    }
}
