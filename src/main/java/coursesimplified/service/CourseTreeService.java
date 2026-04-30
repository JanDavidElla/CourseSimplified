package coursesimplified.service;

import coursesimplified.model.Course;
import coursesimplified.model.Major;
import coursesimplified.model.MajorType;
import coursesimplified.repository.CourseRepository;

public class CourseTreeService {
    private final CourseRepository repository;
    private final CompletionService completionService;
    private Major currentMajor;

    public CourseTreeService(CourseRepository repository, CompletionService completionService) {
        this.repository = repository;
        this.completionService = completionService;
    }

    public Major loadMajor(MajorType type) {
        var graph = repository.loadCourseGraph(type);
        for (Course course : graph.getAllCourses()) {
            course.setCompleted(completionService.isCompleted(course.getCourseCode()));
        }
        currentMajor = new Major(type, graph);
        return currentMajor;
    }

    public void markCourseCompleted(String courseCode) {
        completionService.markCompleted(courseCode);
        refreshCourseFlag(courseCode, true);
    }

    public void markCourseIncomplete(String courseCode) {
        completionService.markIncomplete(courseCode);
        refreshCourseFlag(courseCode, false);
    }

    public Major getCurrentMajor() {
        return currentMajor;
    }

    private void refreshCourseFlag(String courseCode, boolean completed) {
        if (currentMajor == null) return;
        currentMajor.getCourseGraph()
                .getCourse(courseCode)
                .ifPresent(c -> c.setCompleted(completed));
    }
}
