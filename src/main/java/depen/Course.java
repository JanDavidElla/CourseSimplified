package depen;
import java.util.List;
public class Course {
    private int totalUnits;
    private List<Course> prerequisites;
    private boolean completed;
    

    public Course(int totalUnits, List<Course> prerequisites) {
        this.totalUnits = totalUnits;
        this.prerequisites = prerequisites;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public List<Course> getPrerequisites() {
        return prerequisites;
    }


}
