package coursesimplified.gui;

import coursesimplified.model.CourseGraph;
import coursesimplified.model.CourseStatus;
import coursesimplified.model.Major;
import coursesimplified.model.MajorType;
import coursesimplified.service.CourseTreeService;
import coursesimplified.service.UserService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * JavaFX controller that handles UI events and delegates roadmap and course
 * status operations to {@link CourseTreeService}.
 */
public class CourseSimplifiedController {
    private final CourseTreeService service;
    private final UserService userService;
    private final CourseTreeViewBuilder treeViewBuilder;
    private Runnable logoutHandler = () -> { };

    private final ComboBox<MajorType> majorSelector = new ComboBox<>();
    private final Button loadRoadmapButton = new Button("Load Roadmap");
    private final Label progressLabel = new Label("0 of 0 courses completed");
    private final Label remainingLabel = new Label("Remaining: -");
    private final Label inProgressLabel = new Label("In Progress: -");
    private final Label completedLabel = new Label("Completed: -");
    private final TreeView<String> roadmapTree = new TreeView<>();
    private final TextField courseInput = new TextField();
    private final ComboBox<CourseStatus> statusSelector = new ComboBox<>();
    private final Button updateStatusButton = new Button("Update Status");
    private final Label feedbackLabel = new Label("Select a major and load a roadmap to begin.");

    public CourseSimplifiedController(CourseTreeService service) {
        this(service, null);
    }

    public CourseSimplifiedController(CourseTreeService service, UserService userService) {
        this.service = service;
        this.userService = userService;
        this.treeViewBuilder = new CourseTreeViewBuilder();
    }

    public void setLogoutHandler(Runnable logoutHandler) {
        this.logoutHandler = logoutHandler == null ? () -> { } : logoutHandler;
    }

    public Parent createView() {
        Label majorLabel = new Label("Select a Major");
        majorLabel.getStyleClass().add("sidebar-section-label");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button-secondary");
        logoutButton.setOnAction(event -> {
            if (userService != null) {
                userService.logout();
            }
            logoutHandler.run();
        });
        logoutButton.setPrefWidth(112);
        logoutButton.setMinWidth(112);

        HBox selectorRow = new HBox(10, majorSelector, loadRoadmapButton);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(majorSelector, Priority.ALWAYS);

        progressLabel.getStyleClass().add("progress-meta-label");
        remainingLabel.getStyleClass().add("remaining-summary-label");
        inProgressLabel.getStyleClass().add("in-progress-summary-label");
        completedLabel.getStyleClass().add("completed-summary-label");

        progressLabel.getStyleClass().add("sidebar-summary-value");
        remainingLabel.getStyleClass().add("sidebar-summary-value");
        inProgressLabel.getStyleClass().add("sidebar-summary-value");
        completedLabel.getStyleClass().add("sidebar-summary-value");

        Label summarySectionLabel = new Label("Progress snapshot");
        summarySectionLabel.getStyleClass().add("sidebar-section-label");

        HBox summaryRowOne = new HBox(
                10,
                createSummaryCard("Completion", progressLabel, "summary-card-neutral"),
                createSummaryCard("Completed", completedLabel, "summary-card-completed")
        );
        summaryRowOne.getStyleClass().add("summary-grid-row");

        HBox summaryRowTwo = new HBox(
                10,
                createSummaryCard("In Progress", inProgressLabel, "summary-card-in-progress"),
                createSummaryCard("Remaining", remainingLabel, "summary-card-remaining")
        );
        summaryRowTwo.getStyleClass().add("summary-grid-row");

        VBox summaryStack = new VBox(10, summaryRowOne, summaryRowTwo);
        summaryStack.getStyleClass().add("summary-stack");

        VBox topPanel = new VBox(16, majorLabel, selectorRow, summarySectionLabel, summaryStack);
        topPanel.getStyleClass().addAll("panel", "top-panel", "sidebar-panel", "sidebar-hero-panel");
        topPanel.setPrefWidth(360);
        topPanel.setMinWidth(320);

        Label roadmapLabel = new Label("Course Roadmap");
        roadmapLabel.getStyleClass().add("section-title");
        Region roadmapSpacer = new Region();
        HBox.setHgrow(roadmapSpacer, Priority.ALWAYS);

        HBox roadmapHeader = new HBox(12, roadmapLabel, roadmapSpacer, logoutButton);
        roadmapHeader.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(roadmapTree, Priority.ALWAYS);

        VBox centerPanel = new VBox(14, roadmapHeader, roadmapTree);
        centerPanel.getStyleClass().addAll("panel", "roadmap-panel");
        VBox.setVgrow(centerPanel, Priority.ALWAYS);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        centerPanel.setPrefWidth(640);

        Label bottomTitleLabel = new Label("Update Course Status");
        bottomTitleLabel.getStyleClass().add("section-title");
        Label bottomSubtitleLabel = new Label("Apply a status change, then review the refreshed roadmap on the right.");
        bottomSubtitleLabel.getStyleClass().add("sidebar-helper-label");
        bottomSubtitleLabel.setWrapText(true);

        Label courseIdLabel = new Label("Course ID");
        courseIdLabel.getStyleClass().add("field-label");
        VBox courseField = new VBox(6, courseIdLabel, courseInput);
        courseField.getStyleClass().add("field-stack");

        Label statusFieldLabel = new Label("Status");
        statusFieldLabel.getStyleClass().add("field-label");
        VBox statusField = new VBox(6, statusFieldLabel, statusSelector);
        statusField.getStyleClass().add("field-stack");
        statusField.setMaxWidth(Double.MAX_VALUE);

        updateStatusButton.setMaxWidth(Double.MAX_VALUE);

        VBox actionStack = new VBox(12, courseField, statusField, updateStatusButton);
        actionStack.setAlignment(Pos.TOP_LEFT);

        feedbackLabel.getStyleClass().addAll("status-label", "status-info");
        feedbackLabel.setWrapText(true);

        VBox bottomPanel = new VBox(14, bottomTitleLabel, bottomSubtitleLabel, actionStack, feedbackLabel);
        bottomPanel.getStyleClass().addAll("panel", "sidebar-panel", "sidebar-workbench-panel");

        VBox leftColumn = new VBox(14, topPanel, bottomPanel);
        leftColumn.getStyleClass().add("sidebar-column");
        leftColumn.setPrefWidth(360);
        leftColumn.setMinWidth(320);

        HBox contentRow = new HBox(20, leftColumn, centerPanel);
        contentRow.getStyleClass().add("planner-layout");
        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setPadding(new Insets(28));
        root.setCenter(contentRow);

        return root;
    }

    public void initialize() {
        initializeMajorSelector();
        initializeStatusSelector();

        courseInput.setPromptText("Enter course ID (e.g., CS 46A)");

        roadmapTree.setRoot(buildEmptyStateRoot("Load a roadmap to view the prerequisite tree."));
        roadmapTree.setShowRoot(true);
        roadmapTree.setFocusTraversable(false);
        roadmapTree.setCellFactory(tree -> new TreeCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll(
                        "course-completed",
                        "course-in-progress",
                        "course-remaining",
                        "tree-placeholder"
                );

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(item);
                setGraphic(null);

                if (item.startsWith(CourseStatus.Completed.getTreeIcon())) {
                    getStyleClass().add("course-completed");
                } else if (item.startsWith(CourseStatus.InProgress.getTreeIcon())) {
                    getStyleClass().add("course-in-progress");
                } else if (item.startsWith(CourseStatus.Remaining.getTreeIcon())) {
                    getStyleClass().add("course-remaining");
                } else {
                    getStyleClass().add("tree-placeholder");
                }
            }
        });

        loadRoadmapButton.setOnAction(event -> handleLoadRoadmap());
        updateStatusButton.setOnAction(event -> handleUpdateStatus());
        courseInput.setOnAction(event -> handleUpdateStatus());

        updateActionAvailability(false);
    }

    private void initializeMajorSelector() {
        majorSelector.getItems().setAll(MajorType.CS, MajorType.SWE);
        majorSelector.setPromptText("Choose a major");
        majorSelector.setPrefWidth(260);
        majorSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(MajorType majorType) {
                return majorType == null ? "" : toShortLabel(majorType);
            }

            @Override
            public MajorType fromString(String string) {
                throw new UnsupportedOperationException("MajorType conversion is UI-driven only.");
            }
        });
    }

    private void initializeStatusSelector() {
        statusSelector.getItems().setAll(CourseStatus.values());
        statusSelector.setValue(CourseStatus.Completed);
        statusSelector.setPrefWidth(200);
        statusSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(CourseStatus status) {
                return status == null ? "" : status.getDisplayName();
            }

            @Override
            public CourseStatus fromString(String string) {
                throw new UnsupportedOperationException("CourseStatus conversion is UI-driven only.");
            }
        });
    }

    private void handleLoadRoadmap() {
        MajorType selectedMajor = majorSelector.getValue();
        if (selectedMajor == null) {
            showError("Select a major before loading the roadmap.");
            return;
        }

        loadMajorInternal(selectedMajor);
    }

    public void loadMajorProgrammatically(MajorType selectedMajor) {
        if (selectedMajor == null) {
            return;
        }
        majorSelector.setValue(selectedMajor);
        loadMajorInternal(selectedMajor);
    }

    private void loadMajorInternal(MajorType selectedMajor) {
        Major previouslyLoadedMajor = service.getCurrentMajor();
        setBusy(true);
        showInfo("Loading " + toShortLabel(selectedMajor) + " roadmap...");

        // Load roadmap data through the facade so the controller stays UI-only.
        Task<Major> loadTask = new Task<>() {
            @Override
            protected Major call() {
                return service.loadMajor(selectedMajor);
            }
        };

        loadTask.setOnSucceeded(event -> {
            setBusy(false);
            refreshRoadmap(service.getCurrentCourseGraph());
            showSuccess("Loaded " + loadTask.getValue().getMajorName() + ".");
            if (userService != null && userService.hasCurrentUser()) {
                userService.setLastMajorForCurrent(selectedMajor.name());
            }
            courseInput.requestFocus();
        });

        loadTask.setOnFailed(event -> {
            setBusy(false);
            if (previouslyLoadedMajor != null) {
                majorSelector.setValue(previouslyLoadedMajor.getType());
                refreshRoadmap(service.getCurrentCourseGraph());
            } else {
                clearRoadmap();
            }
            // Surface a clear load error in the feedback area instead of failing silently.
            showError("Course data could not be loaded: " + buildErrorMessage(loadTask.getException()));
        });

        Thread loaderThread = new Thread(loadTask, "coursesimplified-roadmap-loader");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    private void handleUpdateStatus() {
        if (!service.hasCurrentMajor()) {
            showError("Load a roadmap before updating course status.");
            return;
        }

        String rawCourseCode = courseInput.getText();
        String normalizedCourseCode = service.normalizeCourseCode(rawCourseCode);
        if (normalizedCourseCode.isBlank()) {
            showError("Enter a course ID before updating status.");
            return;
        }

        CourseStatus selectedStatus = statusSelector.getValue();
        if (selectedStatus == null) {
            showError("Select a status before updating the course.");
            return;
        }

        try {
            // Delegate status transitions and validation to the facade.
            String resultMessage = service.updateCourseStatus(rawCourseCode, selectedStatus);
            refreshRoadmap(service.getCurrentCourseGraph());
            courseInput.clear();
            courseInput.requestFocus();
            showSuccess(resultMessage);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Keep validation and persistence failures visible to the user.
            showError(e.getMessage());
        }
    }

    private void refreshRoadmap(CourseGraph graph) {
        roadmapTree.setRoot(treeViewBuilder.buildTree(graph));
        roadmapTree.setShowRoot(false);
        updateSummary();
        updateActionAvailability(false);
    }

    private void clearRoadmap() {
        roadmapTree.setRoot(buildEmptyStateRoot("Load a roadmap to view the prerequisite tree."));
        roadmapTree.setShowRoot(true);
        progressLabel.setText("0 of 0 courses completed");
        remainingLabel.setText("Remaining: -");
        inProgressLabel.setText("In Progress: -");
        completedLabel.setText("Completed: -");
        updateActionAvailability(false);
    }

    private void setBusy(boolean busy) {
        majorSelector.setDisable(busy);
        loadRoadmapButton.setDisable(busy);
        updateActionAvailability(busy);
    }

    private void updateActionAvailability(boolean busy) {
        boolean canUpdateCourseStatus = !busy && service.hasCurrentMajor();
        courseInput.setDisable(!canUpdateCourseStatus);
        statusSelector.setDisable(!canUpdateCourseStatus);
        updateStatusButton.setDisable(!canUpdateCourseStatus);
    }

    private void showInfo(String message) {
        updateFeedback(message, "status-info");
    }

    private void showSuccess(String message) {
        updateFeedback(message, "status-success");
    }

    private void showError(String message) {
        updateFeedback(message, "status-error");
    }

    private void updateFeedback(String message, String styleClass) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll("status-info", "status-success", "status-error");
        feedbackLabel.getStyleClass().add(styleClass);
    }

    // Rebuild the status summary after each roadmap load or course status update.
    private void updateSummary() {
        long completedCount = service.getCompletedCourseCount();
        long inProgressCount = service.getInProgressCourseCount();
        long remainingCount = service.getRemainingCourseCount();

        progressLabel.setText(completedCount + " of " + service.getTotalCourseCount() + " courses completed");
        remainingLabel.setText("Remaining: " + remainingCount);
        inProgressLabel.setText("In Progress: " + inProgressCount);
        completedLabel.setText("Completed: " + completedCount);
    }

    private String buildErrorMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null) {
            current = current.getCause();
        }
        if (current == null || current.getMessage() == null || current.getMessage().isBlank()) {
            return "Unexpected error while loading the roadmap.";
        }
        return current.getMessage();
    }

    private String toShortLabel(MajorType majorType) {
        return switch (majorType) {
            case CS -> "Computer Science";
            case SWE -> "Software Engineering";
        };
    }

    private TreeItem<String> buildEmptyStateRoot(String message) {
        TreeItem<String> root = new TreeItem<>(message);
        root.setExpanded(true);
        return root;
    }

    private VBox createSummaryCard(String title, Label valueLabel, String modifierClass) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("summary-card-title");

        VBox card = new VBox(4, titleLabel, valueLabel);
        card.getStyleClass().addAll("summary-card", modifierClass);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
}
