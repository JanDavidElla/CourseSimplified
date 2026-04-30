package coursesimplified.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class FileCompletionService implements CompletionService {
    private final Set<String> completedCodes;

    public FileCompletionService(Path filePath) {
        this.completedCodes = new HashSet<>();
        if (Files.exists(filePath)) {
            try {
                Files.lines(filePath)
                        .map(String::trim)
                        .filter(line -> !line.isBlank() && !line.startsWith("#"))
                        .forEach(completedCodes::add);
            } catch (IOException e) {
                System.err.println("Warning: could not read " + filePath + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void markCompleted(String courseCode) {
        completedCodes.add(courseCode.trim());
    }

    @Override
    public void markIncomplete(String courseCode) {
        completedCodes.remove(courseCode.trim());
    }

    @Override
    public boolean isCompleted(String courseCode) {
        return completedCodes.contains(courseCode.trim());
    }

    @Override
    public Set<String> getAllCompleted() {
        return Set.copyOf(completedCodes);
    }
}
