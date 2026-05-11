package coursesimplified.service;

import java.util.Locale;

/**
 * Normalizes course codes and user IDs for consistent storage and comparison.
 * 
 * This class extracts the normalization logic from JsonCompletionService, 
 * promoting the Single Responsibility Principle and enabling reuse across services.
 */
/**
 * One place to normalize course codes and user IDs.
 * 
 * Before: Copy-pasted normalization in 3 different services.
 * After: Put it here so everyone uses the same rules.
 * 
 * Course codes: trim spaces, make uppercase
 * User IDs: trim spaces, make lowercase
 * 
 * Examples: "cs 101 " → "CS 101", "  Alice  " → "alice"
 */
public class CourseStatusNormalizer {
    
    /**
     * Makes a course code consistent (trim, uppercase, single spaces).
     * Returns empty string if null.
     */
    public static String normalizeCourseCode(String courseCode) {
        return courseCode == null ? "" : courseCode.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    /**
     * Makes a user ID consistent (trim and lowercase).
     * Returns empty string if null.
     */
    public static String normalizeUserId(String userId) {
        return userId == null ? "" : userId.trim().toLowerCase(Locale.ROOT);
    }
}
