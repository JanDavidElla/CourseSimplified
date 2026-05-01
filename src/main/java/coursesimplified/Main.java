package coursesimplified;

import coursesimplified.cli.CliRunner;
import coursesimplified.display.ConsoleTreeDisplay;
import coursesimplified.display.CourseTreeDisplay;
import coursesimplified.service.CourseTreeService;

public class Main {
    public static void main(String[] args) {
        CourseTreeService service = CourseSimplifiedBootstrap.createCourseTreeService();
        CourseTreeDisplay display = new ConsoleTreeDisplay();
        new CliRunner(service, display).run();
    }
}
