package coursesimplified.service;

import coursesimplified.model.CourseStatus;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return getAllStatuses().entrySet().stream()
                .filter(entry -> entry.getValue() == CourseStatus.Completed)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }
}
