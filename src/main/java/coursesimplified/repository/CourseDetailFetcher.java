package coursesimplified.repository;

import coursesimplified.api.ApiException;
import coursesimplified.api.CourseApiClient;
import coursesimplified.api.dto.CourseDetailDto;

/**
 * Fetches course info from the API.
 * 
 * If the API call fails, just uses the course code as the name.
 * Keeps API stuff separated from the rest of the code.
 */
public class CourseDetailFetcher {
    private final CourseApiClient client;

    public CourseDetailFetcher(CourseApiClient client) {
        this.client = client;
    }

    /**
     * Fetch course details. If API fails, just use the course code.
     */
    public CourseDetailInfo fetchDetails(String courseCode) {
        try {
            CourseDetailDto detail = client.fetchCourseDetail(courseCode);
            if (detail != null) {
                String courseName = extractShortName(detail.courseName(), courseCode);
                int units = parseUnits(detail.units());
                return new CourseDetailInfo(courseName, units);
            }
        } catch (ApiException e) {
            // Detail fetch failed — use defaults
        }
        
        // Fallback: use course code as name, no units
        return new CourseDetailInfo(courseCode, 0);
    }

    /**
     * Extract the short course name from a full name string.
     * 
     * Handles API format: "CS 101 - Introduction to Computer Science"
     * Returns: "Introduction to Computer Science"
     * 
     * @param fullCourseName the full course name from the API
     * @param courseCode fallback if no dash is found
     * @return the short course name
     */
    private String extractShortName(String fullCourseName, String courseCode) {
        if (fullCourseName == null || fullCourseName.isBlank()) {
            return courseCode;
        }
        
        // API sometimes uses non-breaking spaces around the dash separator.
        String normalized = fullCourseName.replace('\u00A0', ' ');
        int dashIndex = normalized.indexOf(" - ");
        if (dashIndex != -1) {
            return normalized.substring(dashIndex + 3).trim();
        }
        return normalized;
    }

    /**
     * Parse units from a string with fallback to 0.
     * 
     * @param units the units string (may be null or non-numeric)
     * @return the parsed units, or 0 if parsing fails
     */
    private int parseUnits(String units) {
        if (units == null || units.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(units.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Immutable tuple containing course name and unit count.
     */
    public record CourseDetailInfo(String courseName, int units) {}
}
