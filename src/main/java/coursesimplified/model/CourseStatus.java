package coursesimplified.model;

/**
 * Course workflow states: Remaining, In Progress, or Completed.
 * Shown in the UI with icons.
 */
public enum CourseStatus {
    Remaining("Remaining", "❌", "[ ]"),
    InProgress("In Progress", "⏳", "[~]"),
    Completed("Completed", "✅", "[X]");

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
        return input.trim().toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
    }
}
