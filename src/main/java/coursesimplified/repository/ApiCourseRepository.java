package coursesimplified.repository;

import java.util.EnumMap;
import java.util.Map;

import coursesimplified.api.CourseApiClient;
import coursesimplified.model.CourseGraph;
import coursesimplified.model.MajorType;

/**
 * Loads course graphs from the API.
 * 
 * Caches results so we don't hammer the API with the same request twice.
 * Delegates the real work to CourseDetailFetcher and CourseGraphBuilder.
 */
public class ApiCourseRepository implements CourseRepository {
    private final CourseApiClient client;
    private final CourseDetailFetcher detailFetcher;
    private final CourseGraphBuilder graphBuilder;
    private final Map<MajorType, CourseGraph> cache = new EnumMap<>(MajorType.class);

    public ApiCourseRepository(CourseApiClient client) {
        this.client = client;
        this.detailFetcher = new CourseDetailFetcher(client);
        this.graphBuilder = new CourseGraphBuilder(detailFetcher);
    }

    /**
     * Load a course graph. If already loaded, return the cached version.
     */
    @Override
    public CourseGraph loadCourseGraph(MajorType major) {
        // Check cache first. If not there, fetch from API and cache it.
        return cache.computeIfAbsent(major, m -> 
            graphBuilder.buildGraph(m, client.fetchCourseTree(m.getPoid()))
        );
    }
}
