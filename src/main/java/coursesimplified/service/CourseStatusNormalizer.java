package coursesimplified.service;

import java.util.Locale;

/**
 * Normalizes course codes and user IDs for consistent storage and comparison.
 * 
 * This class extracts the normalization logic from JsonCompletionService, 
 * promoting the Single Responsibility Principle and enabling reuse across services.
 */
public class CourseStatusNormalizer {
    
    /**
     * Normalize a course code for consistent storage and lookup.
     * Normalizes whitespace, converts to uppercase, and trims.
     * 
     * @param courseCode the course code to normalize (may be null)
     * @return the normalized course code (empty string if input is null)
     */
    public static String normalizeCourseCode(String courseCode) {
        return courseCode == null ? "" : courseCode.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    /**
     * Normalize a user ID for consistent storage and comparison.
     * Converts to lowercase and trims.
     * 
     * @param userId the user ID to normalize (may be null)
     * @return the normalized user ID (empty string if input is null)
     */
    public static String normalizeUserId(String userId) {
        return userId == null ? "" : userId.trim().toLowerCase(Locale.ROOT);
    }
}
