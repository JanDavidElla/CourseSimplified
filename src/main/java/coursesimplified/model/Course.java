package coursesimplified.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private final String courseCode;
    private final String courseName;
    private final int totalUnits;
    private final List<Course> prerequisites;
    private final boolean isRoot;
    private final boolean isLeaf;
    private CourseStatus status;

    private Course(Builder builder) {
        this.courseCode = builder.courseCode;
        this.courseName = builder.courseName;
        this.totalUnits = builder.totalUnits;
        this.prerequisites = builder.prerequisites;
        this.isRoot = builder.isRoot;
        this.isLeaf = builder.isLeaf;
        this.status = builder.status;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getTotalUnits() { return totalUnits; }
    public List<Course> getPrerequisites() { return prerequisites; }
    public boolean isRoot() { return isRoot; }
    public boolean isLeaf() { return isLeaf; }
    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status == null ? CourseStatus.Remaining : status; }
    public boolean isCompleted() { return status == CourseStatus.Completed; }
    public boolean isInProgress() { return status == CourseStatus.InProgress; }
    public void setCompleted(boolean completed) { this.status = completed ? CourseStatus.Completed : CourseStatus.Remaining; }

    @Override
    public String toString() {
        return status.getConsoleMarker() + " " + courseCode + " - " + courseName + " (" + formatUnits(totalUnits) + ")";
    }

    private String formatUnits(int units) {
        return units == 1 ? "1 unit" : units + " units";
    }

    public static class Builder {
        private String courseCode;
        private String courseName = "";
        private int totalUnits = 0;
        private List<Course> prerequisites = new ArrayList<>();
        private boolean isRoot = false;
        private boolean isLeaf = false;
        private CourseStatus status = CourseStatus.Remaining;

        public Builder courseCode(String courseCode) { this.courseCode = courseCode; return this; }
        public Builder courseName(String courseName) { this.courseName = courseName; return this; }
        public Builder totalUnits(int totalUnits) { this.totalUnits = totalUnits; return this; }
        public Builder prerequisites(List<Course> prerequisites) { this.prerequisites = prerequisites; return this; }
        public Builder isRoot(boolean isRoot) { this.isRoot = isRoot; return this; }
        public Builder isLeaf(boolean isLeaf) { this.isLeaf = isLeaf; return this; }
        public Builder completed(boolean completed) { this.status = completed ? CourseStatus.Completed : CourseStatus.Remaining; return this; }
        public Builder status(CourseStatus status) { this.status = status == null ? CourseStatus.Remaining : status; return this; }

        public Course build() {
            if (courseCode == null || courseCode.isBlank()) {
                throw new IllegalStateException("courseCode is required");
            }
            return new Course(this);
        }
    }
}
