package coursesimplified.service;

import coursesimplified.model.Course;
import coursesimplified.model.CourseGraph;
import coursesimplified.model.CourseStatus;
import coursesimplified.model.Major;
import coursesimplified.model.MajorType;
import coursesimplified.repository.CourseRepository;

import java.util.Locale;
import java.util.Optional;

/**
 * Facade service that provides a simplified interface for loading roadmaps,
 * updating course statuses, and retrieving progress data.
 * <p>
 * The GUI and CLI call this class instead of coordinating repository access,
 * course graph updates, completion persistence, and roadmap selection
 * directly.
 */
public class CourseTreeService {
    private final CourseRepository repository;
    private final CompletionService completionService;
    private Major currentMajor;

    public CourseTreeService(CourseRepository repository, CompletionService completionService) {
        this.repository = repository;
        this.completionService = completionService;
    }

    /**
     * Loads and selects the roadmap for the requested major, then applies any
     * persisted course statuses to the in-memory graph used by the UI layers.
     */
    public Major loadMajor(MajorType type) {
        var graph = repository.loadCourseGraph(type);
        for (Course course : graph.getAllCourses()) {
            course.setStatus(completionService.getStatus(course.getCourseCode()));
        }
        currentMajor = new Major(type, graph);
        return currentMajor;
    }

    public void markCourseCompleted(String courseCode) {
        updateCourseStatus(courseCode, CourseStatus.Completed);
    }

    public void markCourseIncomplete(String courseCode) {
        updateCourseStatus(courseCode, CourseStatus.Remaining);
    }

    /**
     * Validates the course against the currently selected roadmap, persists the
     * requested status, and updates the in-memory course used by the GUI and
     * CLI.
     */
    public String updateCourseStatus(String courseCode, CourseStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Course status is required.");
        }

        Course course = requireCourseInCurrentMajor(courseCode);
        completionService.updateStatus(course.getCourseCode(), status);
        course.setStatus(status);
        return course.getCourseCode() + " updated to " + status.getDisplayName();
    }

    public Major getCurrentMajor() {
        return currentMajor;
    }

    /**
     * Returns the currently selected roadmap graph for rendering or summary
     * display.
     */
    public CourseGraph getCurrentCourseGraph() {
        return requireCurrentMajor().getCourseGraph();
    }

    public Optional<Course> findCourseInCurrentMajor(String courseCode) {
        if (currentMajor == null) {
            return Optional.empty();
        }

        String normalizedCourseCode = normalizeCourseCode(courseCode);
        if (normalizedCourseCode.isBlank()) {
            return Optional.empty();
        }

        return currentMajor.getCourseGraph()
                .getAllCourses()
                .stream()
                .filter(course -> normalizeCourseCode(course.getCourseCode()).equals(normalizedCourseCode))
                .findFirst();
    }

    public boolean hasCurrentMajor() {
        return currentMajor != null;
    }

    /**
     * Returns the number of completed courses in the selected roadmap.
     */
    public long getCompletedCourseCount() {
        return getCurrentCourseGraph().getCompletedCount();
    }

    /**
     * Returns the number of in-progress courses in the selected roadmap.
     */
    public long getInProgressCourseCount() {
        return getCurrentCourseGraph().getInProgressCount();
    }

    /**
     * Returns the number of remaining courses in the selected roadmap.
     */
    public long getRemainingCourseCount() {
        return getTotalCourseCount() - getCompletedCourseCount() - getInProgressCourseCount();
    }

    /**
     * Returns the total number of courses in the selected roadmap.
     */
    public int getTotalCourseCount() {
        return getCurrentCourseGraph().getTotalCount();
    }

    public String normalizeCourseCode(String courseCode) {
        if (courseCode == null) {
            return "";
        }
        return courseCode.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private Major requireCurrentMajor() {
        if (currentMajor == null) {
            throw new IllegalStateException("No major selected.");
        }
        return currentMajor;
    }

    private Course requireCourseInCurrentMajor(String courseCode) {
        requireCurrentMajor();
        String normalizedCourseCode = normalizeCourseCode(courseCode);
        return findCourseInCurrentMajor(courseCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course '" + normalizedCourseCode + "' not found in current major."
                ));
    }
}
