package coursesimplified.cli;

import coursesimplified.display.CourseTreeDisplay;
import coursesimplified.model.Major;
import coursesimplified.service.CourseTreeService;

import java.util.Scanner;

public class CliRunner {
    private final CourseTreeService service;
    private final CourseTreeDisplay display;
    private final CommandParser parser;
    private final Scanner scanner;

    public CliRunner(CourseTreeService service, CourseTreeDisplay display) {
        this.service = service;
        this.display = display;
        this.parser = new CommandParser();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        display.renderMessage("╔══════════════════════════════════════════════════════════╗");
        display.renderMessage("║              CourseSimplified — SJSU Planner             ║");
        display.renderMessage("╚══════════════════════════════════════════════════════════╝");
        display.renderMessage("  Commands:  select cs | select swe | tree | complete | help | quit");
        display.renderMessage("");

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String line = scanner.hasNextLine() ? scanner.nextLine() : null;
            Command command = parser.parse(line);

            if (command instanceof Command.SelectMajor sm) {
                display.renderMessage("Loading " + sm.type().getDisplayName() + "...");
                try {
                    Major major = service.loadMajor(sm.type());
                    display.renderMessage("Loaded " + major.getMajorName() + "  (" + major.getCourseGraph().getTotalCount() + " courses)");
                    display.renderMessage("Type 'tree' to view the course tree.");
                } catch (Exception e) {
                    display.renderError("Failed to load major: " + e.getMessage());
                }
            } else if (command instanceof Command.MarkComplete mc) {
                if (service.getCurrentMajor() == null) {
                    display.renderError("No major selected. Use 'select cs' or 'select swe' first.");
                } else if (service.getCurrentMajor().getCourseGraph().getCourse(mc.courseCode()).isEmpty()) {
                    display.renderError("Course '" + mc.courseCode() + "' not found in current major.");
                } else {
                    service.markCourseCompleted(mc.courseCode());
                    display.renderMessage("✓ " + mc.courseCode() + " marked as completed and saved.");
                }
            } else if (command instanceof Command.MarkIncomplete mi) {
                if (service.getCurrentMajor() == null) {
                    display.renderError("No major selected. Use 'select cs' or 'select swe' first.");
                } else {
                    service.markCourseIncomplete(mi.courseCode());
                    display.renderMessage("✗ " + mi.courseCode() + " marked as incomplete and saved.");
                }
            } else if (command instanceof Command.ShowTree) {
                Major current = service.getCurrentMajor();
                if (current == null) {
                    display.renderError("No major selected. Use 'select cs' or 'select swe' first.");
                } else {
                    display.render(current.getCourseGraph());
                }
            } else if (command instanceof Command.ShowHelp) {
                printHelp();
            } else if (command instanceof Command.Quit) {
                display.renderMessage("Goodbye!");
                running = false;
            } else if (command instanceof Command.Unknown u && !u.input().isBlank()) {
                display.renderError("Unknown command: '" + u.input() + "'. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        display.renderMessage("");
        display.renderMessage("  Available commands:");
        display.renderMessage("    select cs               — load Computer Science, BS");
        display.renderMessage("    select swe              — load Software Engineering, BS");
        display.renderMessage("    tree                    — display the course prerequisite tree");
        display.renderMessage("    complete <code>         — mark a course as completed  (e.g. complete CS 46A)");
        display.renderMessage("    uncomplete <code>       — mark a course as not completed");
        display.renderMessage("    help                    — show this help");
        display.renderMessage("    quit                    — exit the program");
        display.renderMessage("");
        display.renderMessage("  Changes are saved automatically to completed.json.");
        display.renderMessage("");
    }
}
