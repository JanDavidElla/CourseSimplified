package coursesimplified.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

import coursesimplified.model.CourseStatus;

/**
 * Handles serialization of course completion statuses to JSON files.
 * 
 * Encapsulates the logic for converting in-memory status maps to persisted JSON,
 * using a consistent flat format with nested users.
 */
public class CourseStatusSerializer {
    private final Gson gson;

    public CourseStatusSerializer(Gson gson) {
        this.gson = gson;
    }

    /**
     * Serialize statuses to a JSON file.
     * 
     * @param filePath the path where to write the JSON file
     * @param statusesByUserId map of userId → (courseCode → CourseStatus)
     * @throws IllegalStateException if the file cannot be written
     */
    public void serializeToFile(Path filePath, Map<String, Map<String, CourseStatus>> statusesByUserId) {
        try {
            Map<String, Map<String, String>> serializedStatuses = new TreeMap<>();
            
            for (Map.Entry<String, Map<String, CourseStatus>> userEntry : statusesByUserId.entrySet()) {
                Map<String, String> serializedUserStatuses = new TreeMap<>();
                
                for (Map.Entry<String, CourseStatus> statusEntry : userEntry.getValue().entrySet()) {
                    serializedUserStatuses.put(statusEntry.getKey(), statusEntry.getValue().name());
                }
                
                serializedStatuses.put(userEntry.getKey(), serializedUserStatuses);
            }
            
            String json = gson.toJson(serializedStatuses);
            Files.writeString(filePath, json);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save course statuses: " + e.getMessage(), e);
        }
    }
}
