package coursesimplified.model;

public enum MajorType {
    CS("13772", "Computer Science, BS"),
    SWE("13938", "Software Engineering, BS");

    private final String poid;
    private final String displayName;

    MajorType(String poid, String displayName) {
        this.poid = poid;
        this.displayName = displayName;
    }

    public String getPoid() { return poid; }
    public String getDisplayName() { return displayName; }

    public static MajorType fromInput(String input) {
        return switch (input.trim().toLowerCase()) {
            case "cs" -> CS;
            case "swe" -> SWE;
            default -> throw new IllegalArgumentException("Unknown major: '" + input + "'. Use 'cs' or 'swe'.");
        };
    }
}
