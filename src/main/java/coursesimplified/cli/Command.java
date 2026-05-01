package coursesimplified.cli;

import coursesimplified.model.CourseStatus;
import coursesimplified.model.MajorType;

public sealed interface Command permits
        Command.SelectMajor,
        Command.MarkComplete,
        Command.MarkIncomplete,
        Command.UpdateStatus,
        Command.ShowTree,
        Command.ShowHelp,
        Command.Quit,
        Command.Unknown {

    record SelectMajor(MajorType type) implements Command {}
    record MarkComplete(String courseCode) implements Command {}
    record MarkIncomplete(String courseCode) implements Command {}
    record UpdateStatus(String courseCode, CourseStatus status) implements Command {}
    record ShowTree() implements Command {}
    record ShowHelp() implements Command {}
    record Quit() implements Command {}
    record Unknown(String input) implements Command {}
}
