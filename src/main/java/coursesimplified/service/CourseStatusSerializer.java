package coursesimplified.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

import coursesimplified.model.CourseStatus;

/**
 * Saves course statuses to a JSON file.
 * Takes the in-memory map and writes it out.
 */
public class CourseStatusSerializer {
    private final Gson gson;

    public CourseStatusSerializer(Gson gson) {
        this.gson = gson;
    }

    /**
     * Write course statuses to a JSON file.
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
