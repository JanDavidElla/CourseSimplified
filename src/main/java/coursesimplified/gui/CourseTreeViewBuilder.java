package coursesimplified.gui;

import coursesimplified.model.Course;
import coursesimplified.model.CourseGraph;
import coursesimplified.model.CourseStatus;
import javafx.scene.control.TreeItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts the loaded course graph into JavaFX {@link TreeItem} nodes and
 * applies visual status markers: {@code ✔} for completed, {@code ◐} for
 * in-progress, and {@code ○} for remaining courses.
 */
public class CourseTreeViewBuilder {

    public TreeItem<String> buildTree(CourseGraph graph) {
        TreeItem<String> root = new TreeItem<>(graph.getMajorType().getDisplayName());
        root.setExpanded(true);

        List<Course> roots = graph.getRootCourses();
        if (roots.isEmpty()) {
            root.getChildren().add(new TreeItem<>("(no courses found)"));
            return root;
        }

        Set<String> visited = new HashSet<>();
        for (Course course : roots) {
            root.getChildren().add(buildNode(course, graph, visited));
        }
        return root;
    }

    private TreeItem<String> buildNode(Course course, CourseGraph graph, Set<String> visited) {
        if (visited.contains(course.getCourseCode())) {
            return new TreeItem<>(formatCourseLabel(course) + " (see above)");
        }

        TreeItem<String> item = new TreeItem<>(formatCourseLabel(course));
        item.setExpanded(true);
        visited.add(course.getCourseCode());

        // The roadmap is a DAG, so repeated courses are shown once and then referenced.
        for (Course dependent : findDependents(course, graph)) {
            item.getChildren().add(buildNode(dependent, graph, visited));
        }
        return item;
    }

    private List<Course> findDependents(Course course, CourseGraph graph) {
        return graph.getAllCourses().stream()
                .filter(candidate -> candidate.getPrerequisites().stream()
                        .anyMatch(prerequisite -> prerequisite.getCourseCode().equals(course.getCourseCode())))
                .toList();
    }

    private String formatCourseLabel(Course course) {
        // Match each course status to the visual marker used in the JavaFX tree.
        String icon = switch (course.getStatus()) {
            case Completed -> CourseStatus.Completed.getTreeIcon();
            case InProgress -> CourseStatus.InProgress.getTreeIcon();
            case Remaining -> CourseStatus.Remaining.getTreeIcon();
        };
        return icon + " " + course.getCourseCode()
                + " - " + course.getCourseName()
                + " (" + formatUnits(course.getTotalUnits()) + ")";
    }

    private String formatUnits(int units) {
        return units == 1 ? "1 unit" : units + " units";
    }
}
