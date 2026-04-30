package coursesimplified.service;

import java.util.Set;

public interface CompletionService {
    void markCompleted(String courseCode);
    void markIncomplete(String courseCode);
    boolean isCompleted(String courseCode);
    Set<String> getAllCompleted();
}
