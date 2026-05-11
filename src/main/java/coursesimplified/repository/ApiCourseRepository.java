package coursesimplified.repository;

import java.util.EnumMap;
import java.util.Map;

import coursesimplified.api.CourseApiClient;
import coursesimplified.model.CourseGraph;
import coursesimplified.model.MajorType;

/**
 * Repository for loading course graphs from the API.
 * 
 * This repository coordinates the transformation from API DTOs to domain models,
 * delegating to specialized components:
 * - CourseDetailFetcher: Handles API calls and detail enrichment
 * - CourseGraphBuilder: Transforms DTOs to CourseGraph
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

    @Override
    public CourseGraph loadCourseGraph(MajorType major) {
        return cache.computeIfAbsent(major, m -> graphBuilder.buildGraph(m, client.fetchCourseTree(m.getPoid())));
    }
}
