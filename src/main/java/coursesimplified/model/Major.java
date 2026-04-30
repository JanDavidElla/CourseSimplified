package coursesimplified.model;

public class Major {
    private final MajorType type;
    private final CourseGraph courseGraph;

    public Major(MajorType type, CourseGraph courseGraph) {
        this.type = type;
        this.courseGraph = courseGraph;
    }

    public MajorType getType() { return type; }
    public CourseGraph getCourseGraph() { return courseGraph; }
    public String getMajorName() { return type.getDisplayName(); }
}
