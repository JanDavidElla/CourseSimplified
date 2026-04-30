package coursesimplified.display;

import coursesimplified.model.CourseGraph;

public interface CourseTreeDisplay {
    void render(CourseGraph graph);
    void renderSummary(CourseGraph graph);
    void renderError(String message);
    void renderMessage(String message);
}
