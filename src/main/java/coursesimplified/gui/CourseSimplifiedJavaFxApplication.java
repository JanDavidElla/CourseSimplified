package coursesimplified.gui;

import java.util.Objects;

import coursesimplified.CourseSimplifiedBootstrap;
import coursesimplified.model.User;
import coursesimplified.service.UserService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CourseSimplifiedJavaFxApplication extends Application {
    private static final String WINDOW_TITLE = "CourseSimplified \u2013 SJSU Planner";

    private Stage primaryStage;
    private final UserService userService = CourseSimplifiedBootstrap.createUserService();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLoginScene();
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.show();
    }

    private void showLoginScene() {
        LoginController loginController = new LoginController(userService);
        loginController.setLoginSuccessHandler(this::showPlannerScene);

        Scene scene = new Scene(loginController.createView(), 720, 520);
        applyStylesheet(scene);

        primaryStage.setTitle("CourseSimplified - Login");
        primaryStage.setScene(scene);
    }

    private void showPlannerScene(User user) {
        var service = CourseSimplifiedBootstrap.createCourseTreeService(user.getUserId());
        CourseSimplifiedController controller = new CourseSimplifiedController(service, userService);
        controller.setLogoutHandler(() -> {
            userService.logout();
            showLoginScene();
        });

        Scene scene = new Scene(controller.createView(), 980, 720);
        applyStylesheet(scene);

        controller.initialize();

        // Auto-load the last selected major for this user if present
        String lastMajor = userService.getLastMajorForCurrent();
        if (lastMajor != null && !lastMajor.isBlank()) {
            try {
                var m = coursesimplified.model.MajorType.valueOf(lastMajor);
                controller.loadMajorProgrammatically(m); //Used to preload last major opened by user.
            } catch (IllegalArgumentException ignored) {
                // ignore invalid stored value
            }
        }

        primaryStage.setTitle(WINDOW_TITLE + " - " + user.getUsername());
        primaryStage.setScene(scene);
    }

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/coursesimplified/gui/coursesimplified.css")
        ).toExternalForm());
    }
}
