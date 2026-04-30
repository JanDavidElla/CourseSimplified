package coursesimplified.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CourseGraph {
    private final MajorType majorType;
    private final Map<String, Course> coursesByCourseCode;
    private final List<CourseEdge> edges;

    public record CourseEdge(String source, String target) {}

    public CourseGraph(MajorType majorType) {
        this.majorType = majorType;
        this.coursesByCourseCode = new LinkedHashMap<>();
        this.edges = new ArrayList<>();
    }

    public void addCourse(Course course) {
        coursesByCourseCode.put(course.getCourseCode(), course);
    }

    public void addEdge(String sourceCode, String targetCode) {
        edges.add(new CourseEdge(sourceCode, targetCode));
    }

    public Optional<Course> getCourse(String courseCode) {
        return Optional.ofNullable(coursesByCourseCode.get(courseCode));
    }

    public Collection<Course> getAllCourses() {
        return coursesByCourseCode.values();
    }

    public List<Course> getRootCourses() {
        return coursesByCourseCode.values().stream()
                .filter(Course::isRoot)
                .toList();
    }

    public List<CourseEdge> getEdges() {
        return edges;
    }

    public MajorType getMajorType() {
        return majorType;
    }

    public long getCompletedCount() {
        return coursesByCourseCode.values().stream().filter(Course::isCompleted).count();
    }

    public int getTotalCount() {
        return coursesByCourseCode.size();
    }
}
