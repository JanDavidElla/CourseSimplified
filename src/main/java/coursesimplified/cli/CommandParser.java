package coursesimplified.cli;

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

        return new Command.Unknown(trimmed);
    }
}
