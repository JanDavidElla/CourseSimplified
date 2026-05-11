package coursesimplified.service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import coursesimplified.model.CourseStatus;

/**
 * Interface for tracking which courses a user has completed/in-progress.
 * Can be backed by JSON files, plain text, database, whatever.
 */
public interface CompletionService {
    void updateStatus(String courseCode, CourseStatus status);
    CourseStatus getStatus(String courseCode);
    Map<String, CourseStatus> getAllStatuses();

    default void markCompleted(String courseCode) {
        updateStatus(courseCode, CourseStatus.Completed);
    }

    default void markIncomplete(String courseCode) {
        updateStatus(courseCode, CourseStatus.Remaining);
    }

    default boolean isCompleted(String courseCode) {
        return getStatus(courseCode) == CourseStatus.Completed;
    }

    default Set<String> getAllCompleted() {
        return getAllStatuses().entrySet().stream().filter(entry -> entry.getValue() == CourseStatus.Completed).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }
}
