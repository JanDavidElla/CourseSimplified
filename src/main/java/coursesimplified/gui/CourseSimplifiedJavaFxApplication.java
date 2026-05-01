package coursesimplified.gui;

import coursesimplified.CourseSimplifiedBootstrap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class CourseSimplifiedJavaFxApplication extends Application {
    private static final String WINDOW_TITLE = "CourseSimplified \u2013 SJSU Planner";

    @Override
    public void start(Stage stage) {
        CourseSimplifiedController controller =
                new CourseSimplifiedController(CourseSimplifiedBootstrap.createCourseTreeService());
        Scene scene = new Scene(controller.createView(), 980, 720);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/coursesimplified/gui/coursesimplified.css")
        ).toExternalForm());

        controller.initialize();

        stage.setTitle(WINDOW_TITLE);
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }
}
