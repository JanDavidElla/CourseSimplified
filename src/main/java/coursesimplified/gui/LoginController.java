package coursesimplified.gui;

import coursesimplified.model.User;
import coursesimplified.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builds the JavaFX authentication screen and delegates account operations to
 * {@link UserService}.
 */
public class LoginController {
    private final UserService userService;
    private Consumer<User> loginSuccessHandler = user -> { };

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label feedbackLabel = new Label("Sign in or create an account to keep roadmap progress separated by user.");

    public LoginController(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    public void setLoginSuccessHandler(Consumer<User> loginSuccessHandler) {
        this.loginSuccessHandler = loginSuccessHandler == null ? user -> { } : loginSuccessHandler;
    }

    public Parent createView() {
        Label heroKicker = new Label("SJSU roadmap planning");
        heroKicker.getStyleClass().add("login-kicker");

        Label heroTitle = new Label("Track degree\nprogress");
        heroTitle.getStyleClass().add("login-hero-title");
        heroTitle.setWrapText(true);

        Label heroBody = new Label(
                "Save your roadmap selections, revisit your last major, and keep course updates."
        );
        heroBody.getStyleClass().add("login-hero-body");
        heroBody.setWrapText(true);

        VBox featureStack = new VBox(
                12,
                createFeatureChip("Per-user progress", "Course statuses stay scoped to your account."),
                createFeatureChip("Secure credentials", "Passwords are hashed before they are stored."),
                createFeatureChip("Fast resume", "Your last-opened major is restored after login.")
        );
        featureStack.getStyleClass().add("login-feature-stack");

        VBox heroPanel = new VBox(18, heroKicker, heroTitle, heroBody, featureStack);
        heroPanel.getStyleClass().addAll("panel", "login-hero");
        heroPanel.setMinWidth(400);
        heroPanel.setPrefWidth(460);
        heroPanel.setMaxWidth(480);

        Label formKicker = new Label("Account access");
        formKicker.getStyleClass().add("login-form-kicker");

        Label formTitle = new Label("Welcome back");
        formTitle.getStyleClass().add("login-form-title");
        formTitle.setWrapText(true);

        Label formSubtitle = new Label("Sign in to continue, or create a new account to start saving roadmap progress.");
        formSubtitle.getStyleClass().add("login-form-body");
        formSubtitle.setWrapText(true);

        usernameField.setPromptText("Enter your username");
        passwordField.setPromptText("Enter your password");

        VBox usernameFieldGroup = new VBox(6, labeledField("Username"), usernameField);
        usernameFieldGroup.getStyleClass().add("login-field-group");

        VBox passwordFieldGroup = new VBox(6, labeledField("Password"), passwordField);
        passwordFieldGroup.getStyleClass().add("login-field-group");

        Button signInButton = new Button("Sign In");
        Button createAccountButton = new Button("Create Account");
        createAccountButton.getStyleClass().add("button-secondary");

        signInButton.setDefaultButton(true);
        signInButton.setOnAction(event -> handleLogin());
        createAccountButton.setOnAction(event -> handleRegister());
        passwordField.setOnAction(event -> handleLogin());

        HBox buttonRow = new HBox(10, signInButton, createAccountButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(signInButton, Priority.ALWAYS);
        HBox.setHgrow(createAccountButton, Priority.ALWAYS);
        signInButton.setMaxWidth(Double.MAX_VALUE);
        createAccountButton.setMaxWidth(Double.MAX_VALUE);

        feedbackLabel.getStyleClass().addAll("status-label", "status-info", "login-feedback");
        feedbackLabel.setWrapText(true);

        VBox formPanel = new VBox(
                16,
                formKicker,
                formTitle,
                formSubtitle,
                usernameFieldGroup,
                passwordFieldGroup,
                buttonRow,
                feedbackLabel
        );
        formPanel.getStyleClass().addAll("panel", "login-card");
        formPanel.setMinWidth(500);
        formPanel.setPrefWidth(560);
        formPanel.setMaxWidth(580);
        HBox.setHgrow(formPanel, Priority.ALWAYS);

        HBox content = new HBox(24, heroPanel, formPanel);
        content.getStyleClass().add("login-layout");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(1120);

        StackPane root = new StackPane(content);
        root.getStyleClass().addAll("app-shell", "login-shell");
        root.setPadding(new Insets(36));
        return root;
    }

    private Label labeledField(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private VBox createFeatureChip(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("login-chip-title");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("login-chip-copy");
        bodyLabel.setWrapText(true);

        VBox chip = new VBox(6, titleLabel, bodyLabel);
        chip.getStyleClass().add("login-chip");
        chip.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(chip, Priority.ALWAYS);
        return chip;
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
            User user = register
                    ? userService.register(username, password)
                    : userService.login(username, password);
            feedbackLabel.getStyleClass().removeAll("status-error");
            feedbackLabel.getStyleClass().add("status-success");
            feedbackLabel.setText("Welcome, " + user.getUsername() + ". Opening your planner...");
            loginSuccessHandler.accept(user);
        } catch (IllegalArgumentException | IllegalStateException e) {
            feedbackLabel.getStyleClass().removeAll("status-success");
            feedbackLabel.getStyleClass().add("status-error");
            feedbackLabel.setText(e.getMessage());
        }
    }
}
