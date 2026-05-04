package coursesimplified.gui;

import java.util.Objects;
import java.util.function.Consumer;

import coursesimplified.model.User;
import coursesimplified.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {
    private final UserService userService;
    private Consumer<User> loginSuccessHandler = user -> {};

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label feedbackLabel = new Label("Sign in or create an account to load your roadmap.");

    public LoginController(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    public void setLoginSuccessHandler(Consumer<User> loginSuccessHandler) {
        this.loginSuccessHandler = loginSuccessHandler == null ? user -> {} : loginSuccessHandler;
    }

    public Parent createView() {
        Label titleLabel = new Label("CourseSimplified Login");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Use a username and password to open your personalized roadmap.");
        subtitleLabel.getStyleClass().add("subtitle-label");

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Log In");
        Button registerButton = new Button("Create Account");

        loginButton.setDefaultButton(true);
        registerButton.setOnAction(event -> handleRegister());
        loginButton.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());

        feedbackLabel.getStyleClass().add("status-label");

        VBox root = new VBox(14, titleLabel, subtitleLabel, usernameField, passwordField, loginButton, registerButton, feedbackLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32));
        root.setMaxWidth(420);
        root.getStyleClass().add("panel");
        return root;
    }

    private void handleLogin() {
        authenticate(false);
    }

    private void handleRegister() {
        authenticate(true);
    }

    private void authenticate(boolean register) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = register ? userService.register(username, password) : userService.login(username, password);
            feedbackLabel.getStyleClass().removeAll("status-error");
            feedbackLabel.getStyleClass().add("status-success");
            feedbackLabel.setText("Welcome, " + user.getUsername() + ". Loading your roadmap...");
            loginSuccessHandler.accept(user);
        } catch (IllegalArgumentException e) {
            feedbackLabel.getStyleClass().removeAll("status-success");
            feedbackLabel.getStyleClass().add("status-error");
            feedbackLabel.setText(e.getMessage());
        }
    }
}