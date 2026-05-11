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
    private static final String LOGIN_TITLE = "CourseSimplified \u2013 Sign In";
    private static final double LOGIN_WIDTH = 1040;
    private static final double LOGIN_HEIGHT = 760;
    private static final double PLANNER_WIDTH = 1120;
    private static final double PLANNER_HEIGHT = 820;

    private final UserService userService = CourseSimplifiedBootstrap.createUserService();
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScene();
        primaryStage.show();
    }

    private void showLoginScene() {
        LoginController loginController = new LoginController(userService);
        loginController.setLoginSuccessHandler(this::showPlannerScene);

        Scene scene = new Scene(loginController.createView(), LOGIN_WIDTH, LOGIN_HEIGHT);
        applyStylesheet(scene);

        primaryStage.setMinWidth(LOGIN_WIDTH);
        primaryStage.setMinHeight(LOGIN_HEIGHT);
        primaryStage.setWidth(LOGIN_WIDTH);
        primaryStage.setHeight(LOGIN_HEIGHT);
        primaryStage.setTitle(LOGIN_TITLE);
        primaryStage.setScene(scene);
    }

    private void showPlannerScene(User user) {
        CourseSimplifiedController controller = new CourseSimplifiedController(
                CourseSimplifiedBootstrap.createCourseTreeService(user.getUserId()),
                userService
        );
        controller.setLogoutHandler(() -> {
            userService.logout();
            showLoginScene();
        });

        Scene scene = new Scene(controller.createView(), PLANNER_WIDTH, PLANNER_HEIGHT);
        applyStylesheet(scene);
        controller.initialize();

        String lastMajor = userService.getLastMajorForCurrent();
        if (lastMajor != null && !lastMajor.isBlank()) {
            try {
                controller.loadMajorProgrammatically(coursesimplified.model.MajorType.valueOf(lastMajor));
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid saved values and leave the planner unloaded.
            }
        }

        primaryStage.setMinWidth(PLANNER_WIDTH);
        primaryStage.setMinHeight(PLANNER_HEIGHT);
        primaryStage.setWidth(PLANNER_WIDTH);
        primaryStage.setHeight(PLANNER_HEIGHT);
        primaryStage.setTitle(WINDOW_TITLE + " \u2013 " + user.getUsername());
        primaryStage.setScene(scene);
    }

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/coursesimplified/gui/coursesimplified.css")
        ).toExternalForm());
    }
}
