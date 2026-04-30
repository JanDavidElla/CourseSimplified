package coursesimplified.display;

import coursesimplified.model.Course;
import coursesimplified.model.CourseGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConsoleTreeDisplay implements CourseTreeDisplay {

    @Override
    public void render(CourseGraph graph) {
        renderSummary(graph);
        System.out.println();

        Set<String> visited = new HashSet<>();
        List<Course> roots = graph.getRootCourses();

        if (roots.isEmpty()) {
            System.out.println("  (no courses found)");
            return;
        }

        for (int i = 0; i < roots.size(); i++) {
            boolean isLast = (i == roots.size() - 1);
            renderNode(roots.get(i), graph, "", isLast, visited);
        }
    }

    @Override
    public void renderSummary(CourseGraph graph) {
        String name = graph.getMajorType().getDisplayName();
        long completed = graph.getCompletedCount();
        int total = graph.getTotalCount();
        System.out.println("═".repeat(60));
        System.out.println("  " + name);
        System.out.printf("  Progress: %d / %d courses completed%n", completed, total);
        System.out.println("═".repeat(60));
    }

    @Override
    public void renderError(String message) {
        System.out.println("[ERROR] " + message);
    }

    @Override
    public void renderMessage(String message) {
        System.out.println(message);
    }

    private void renderNode(Course course, CourseGraph graph, String prefix, boolean isLast, Set<String> visited) {
        String connector = prefix.isEmpty() ? "" : (isLast ? "└── " : "├── ");
        String continuation = isLast ? "    " : "│   ";

        if (visited.contains(course.getCourseCode())) {
            System.out.println(prefix + connector + course + "  ↑ (see above)");
            return;
        }

        System.out.println(prefix + connector + course);
        visited.add(course.getCourseCode());

        // Forward traversal: find courses that list this course as a prerequisite
        List<Course> dependents = graph.getAllCourses().stream()
                .filter(c -> c.getPrerequisites().stream()
                        .anyMatch(p -> p.getCourseCode().equals(course.getCourseCode())))
                .toList();

        for (int i = 0; i < dependents.size(); i++) {
            boolean childIsLast = (i == dependents.size() - 1);
            renderNode(dependents.get(i), graph, prefix + continuation, childIsLast, visited);
        }
    }
}
