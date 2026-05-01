package coursesimplified.service;

import coursesimplified.model.CourseStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FileCompletionService implements CompletionService {
    private final Map<String, CourseStatus> statusesByCode;

    public FileCompletionService(Path filePath) {
        this.statusesByCode = new HashMap<>();
        if (Files.exists(filePath)) {
            try {
                Files.lines(filePath)
                        .map(String::trim)
                        .filter(line -> !line.isBlank() && !line.startsWith("#"))
                        .map(this::normalizeCourseCode)
                        .forEach(courseCode -> statusesByCode.put(courseCode, CourseStatus.Completed));
            } catch (IOException e) {
                System.err.println("Warning: could not read " + filePath + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void updateStatus(String courseCode, CourseStatus status) {
        String normalizedCourseCode = normalizeCourseCode(courseCode);
        if (status == null || status == CourseStatus.Remaining) {
            statusesByCode.remove(normalizedCourseCode);
            return;
        }
        statusesByCode.put(normalizedCourseCode, status);
    }

    @Override
    public CourseStatus getStatus(String courseCode) {
        return statusesByCode.getOrDefault(normalizeCourseCode(courseCode), CourseStatus.Remaining);
    }

    @Override
    public Map<String, CourseStatus> getAllStatuses() {
        return Map.copyOf(statusesByCode);
    }

    @Override
    public Set<String> getAllCompleted() {
        return new HashSet<>(CompletionService.super.getAllCompleted());
    }

    private String normalizeCourseCode(String courseCode) {
        return courseCode == null ? "" : courseCode.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
