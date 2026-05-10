package coursesimplified.service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import coursesimplified.model.CourseStatus;

/**
 * Manages course completion status persistence using JSON files.
 * 
 * This service coordinates the loading and saving of course statuses, delegating
 * to specialized helpers for format detection, serialization, and normalization.
 * Responsibilities:
 * - Coordinate status management operations
 * - Maintain current user context
 * - Delegate to CourseStatusLoader for loading
 * - Delegate to CourseStatusSerializer for saving
 * - Use CourseStatusNormalizer for consistent normalization
 */
public class JsonCompletionService implements CompletionService {
    private final Path filePath;
    private final Gson gson;
    private final String userId;
    private final Map<String, Map<String, CourseStatus>> statusesByUserId;
    private final CourseStatusLoader loader;
    private final CourseStatusSerializer serializer;

    public JsonCompletionService(Path filePath, Gson gson) {
        this(filePath, gson, "cli");
    }

    public JsonCompletionService(Path filePath, Gson gson, String userId) {
        this.filePath = filePath;
        this.gson = gson;
        this.userId = CourseStatusNormalizer.normalizeUserId(userId);
        this.statusesByUserId = new LinkedHashMap<>();
        this.loader = new CourseStatusLoader(gson);
        this.serializer = new CourseStatusSerializer(gson);
        load();
    }

    @Override
    public void updateStatus(String courseCode, CourseStatus status) {
        String normalizedCourseCode = CourseStatusNormalizer.normalizeCourseCode(courseCode);
        Map<String, CourseStatus> currentStatuses = currentStatuses();
        CourseStatus previousStatus = currentStatuses.get(normalizedCourseCode);

        if (status == null || status == CourseStatus.Remaining) {
            currentStatuses.remove(normalizedCourseCode);
        } else {
            currentStatuses.put(normalizedCourseCode, status);
        }

        try {
            save();
        } catch (IllegalStateException e) {
            // Rollback on save failure
            if (previousStatus == null) {
                currentStatuses.remove(normalizedCourseCode);
            } else {
                currentStatuses.put(normalizedCourseCode, previousStatus);
            }
            throw e;
        }
    }

    @Override
    public CourseStatus getStatus(String courseCode) {
        return currentStatuses().getOrDefault(
                CourseStatusNormalizer.normalizeCourseCode(courseCode),
                CourseStatus.Remaining);
    }

    @Override
    public Map<String, CourseStatus> getAllStatuses() {
        return Map.copyOf(currentStatuses());
    }

    @Override
    public Set<String> getAllCompleted() {
        return CompletionService.super.getAllCompleted();
    }

    private void load() {
        statusesByUserId.putAll(loader.loadFromFile(filePath));
    }

    private void save() {
        serializer.serializeToFile(filePath, statusesByUserId);
    }

    private Map<String, CourseStatus> currentStatuses() {
        return statusesByUserId.computeIfAbsent(userId, key -> new LinkedHashMap<>());
    }
}
