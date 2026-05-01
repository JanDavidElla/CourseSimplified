package coursesimplified.model;

/**
 * User-facing course workflow states shown in the CLI and JavaFX planner.
 */
public enum CourseStatus {
    Remaining("Remaining", "\u25cb", "[ ]"),
    InProgress("In Progress", "\u25d0", "[~]"),
    Completed("Completed", "\u2714", "[X]");

    private final String displayName;
    private final String treeIcon;
    private final String consoleMarker;

    CourseStatus(String displayName, String treeIcon, String consoleMarker) {
        this.displayName = displayName;
        this.treeIcon = treeIcon;
        this.consoleMarker = consoleMarker;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTreeIcon() {
        return treeIcon;
    }

    public String getConsoleMarker() {
        return consoleMarker;
    }

    public static CourseStatus fromInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Course status is required.");
        }

        return switch (normalize(input)) {
            case "remaining" -> Remaining;
            case "inprogress" -> InProgress;
            case "completed" -> Completed;
            default -> throw new IllegalArgumentException("Unknown course status: '" + input + "'.");
        };
    }

    private static String normalize(String input) {
        return input.trim()
                .toLowerCase()
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
    }
}
