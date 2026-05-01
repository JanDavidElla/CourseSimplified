package coursesimplified.cli;

import coursesimplified.model.CourseStatus;
import coursesimplified.model.MajorType;

public class CommandParser {

    public Command parse(String rawInput) {
        if (rawInput == null) return new Command.Quit();
        String trimmed = rawInput.trim();
        if (trimmed.isBlank()) return new Command.Unknown("");

        String lower = trimmed.toLowerCase();

        if (lower.equals("tree") || lower.equals("show")) {
            return new Command.ShowTree();
        }
        if (lower.equals("help") || lower.equals("?")) {
            return new Command.ShowHelp();
        }
        if (lower.equals("quit") || lower.equals("exit") || lower.equals("q")) {
            return new Command.Quit();
        }
        if (lower.startsWith("select ")) {
            String arg = trimmed.substring(7).trim();
            try {
                return new Command.SelectMajor(MajorType.fromInput(arg));
            } catch (IllegalArgumentException e) {
                return new Command.Unknown(trimmed);
            }
        }
        if (lower.startsWith("complete ")) {
            String code = trimmed.substring(9).trim();
            return code.isBlank() ? new Command.Unknown(trimmed) : new Command.MarkComplete(code);
        }
        if (lower.startsWith("uncomplete ")) {
            String code = trimmed.substring(11).trim();
            return code.isBlank() ? new Command.Unknown(trimmed) : new Command.MarkIncomplete(code);
        }
        if (lower.startsWith("status ")) {
            return parseStatusCommand(trimmed);
        }

        return new Command.Unknown(trimmed);
    }

    private Command parseStatusCommand(String rawInput) {
        String arguments = rawInput.substring(7).trim();
        if (arguments.isBlank()) {
            return new Command.Unknown(rawInput);
        }

        String[] suffixes = {" in progress", " in-progress", " inprogress", " completed", " remaining"};
        for (String suffix : suffixes) {
            if (arguments.toLowerCase().endsWith(suffix)) {
                String courseCode = arguments.substring(0, arguments.length() - suffix.length()).trim();
                String statusInput = arguments.substring(arguments.length() - suffix.length()).trim();
                if (courseCode.isBlank()) {
                    return new Command.Unknown(rawInput);
                }

                try {
                    return new Command.UpdateStatus(courseCode, CourseStatus.fromInput(statusInput));
                } catch (IllegalArgumentException e) {
                    return new Command.Unknown(rawInput);
                }
            }
        }

        return new Command.Unknown(rawInput);
    }
}
