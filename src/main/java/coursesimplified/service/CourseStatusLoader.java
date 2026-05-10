package coursesimplified.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coursesimplified.model.CourseStatus;

/**
 * Handles loading course completion statuses from JSON files with support
 * for multiple legacy formats.
 * 
 * This class encapsulates format detection and parsing logic, supporting:
 * - Legacy array format: ["CS101", "CS102"]
 * - Legacy object with "completed" field: {"completed": ["CS101", "CS102"]}
 * - Nested by user: {"user1": {"CS101": "Completed", ...}, "user2": {...}}
 * - Flat format: {"CS101": "Completed", "CS102": "InProgress"}
 */
public class CourseStatusLoader {
    private final Gson gson;

    public CourseStatusLoader(Gson gson) {
        this.gson = gson;
    }

    /**
     * Load statuses from a JSON file, auto-detecting the format.
     * Returns a map of userId → courseCode → CourseStatus.
     * 
     * @param filePath the path to the JSON file
     * @return a map of user IDs to their course statuses
     */
    public Map<String, Map<String, CourseStatus>> loadFromFile(Path filePath) {
        Map<String, Map<String, CourseStatus>> statusesByUserId = new LinkedHashMap<>();
        
        if (!Files.exists(filePath)) {
            return statusesByUserId;
        }

        try {
            String json = Files.readString(filePath);
            JsonElement root = gson.fromJson(json, JsonElement.class);
            
            if (root == null || root.isJsonNull()) {
                return statusesByUserId;
            }

            loadFromJsonElement(root, statusesByUserId);
        } catch (IOException | RuntimeException e) {
            System.err.println("Warning: could not read " + filePath + ": " + e.getMessage());
        }
        
        return statusesByUserId;
    }

    private void loadFromJsonElement(JsonElement root, Map<String, Map<String, CourseStatus>> statusesByUserId) {
        if (root.isJsonArray()) {
            // Legacy format: ["CS101", "CS102"]
            loadLegacyCompletedArray(statusesByUserId, root.getAsJsonArray());
            return;
        }

        if (root.isJsonObject()) {
            JsonObject object = root.getAsJsonObject();
            
            // Preserve compatibility with original completed-only JSON shape
            if (object.has("completed") && object.get("completed").isJsonArray()) {
                loadLegacyCompletedArray(statusesByUserId, object.getAsJsonArray("completed"));
                return;
            }

            // Detect if nested by user or flat
            boolean looksNestedByUser = object.entrySet().stream()
                    .anyMatch(entry -> entry.getValue() != null && entry.getValue().isJsonObject());

            if (looksNestedByUser) {
                loadNestedStatuses(object, statusesByUserId);
            } else {
                loadFlatStatuses(statusesByUserId, object, "cli");
            }
        }
    }

    /**
     * Load from legacy array format: ["CS101", "CS102"]
     * Treats all courses as Completed.
     */
    private void loadLegacyCompletedArray(Map<String, Map<String, CourseStatus>> statusesByUserId, 
                                         JsonArray completedArray) {
        Map<String, CourseStatus> targetStatuses = statusesByUserId.computeIfAbsent(
                "cli", 
                key -> new LinkedHashMap<>());
        
        for (JsonElement courseCodeElement : completedArray) {
            if (courseCodeElement.isJsonPrimitive() && courseCodeElement.getAsJsonPrimitive().isString()) {
                String normalizedCourseCode = CourseStatusNormalizer.normalizeCourseCode(
                        courseCodeElement.getAsString());
                targetStatuses.put(normalizedCourseCode, CourseStatus.Completed);
            }
        }
    }

    /**
     * Load from nested by-user format:
     * {"user1": {"CS101": "Completed", ...}, "user2": {...}}
     */
    private void loadNestedStatuses(JsonObject object, Map<String, Map<String, CourseStatus>> statusesByUserId) {
        for (Map.Entry<String, JsonElement> userEntry : object.entrySet()) {
            if (!userEntry.getValue().isJsonObject()) {
                continue;
            }

            Map<String, CourseStatus> targetStatuses = statusesByUserId.computeIfAbsent(
                    CourseStatusNormalizer.normalizeUserId(userEntry.getKey()),
                    key -> new LinkedHashMap<>());
            loadFlatStatuses(statusesByUserId, userEntry.getValue().getAsJsonObject(), 
                           CourseStatusNormalizer.normalizeUserId(userEntry.getKey()));
        }
    }

    /**
     * Load from flat format: {"CS101": "Completed", "CS102": "InProgress"}
     */
    private void loadFlatStatuses(Map<String, Map<String, CourseStatus>> statusesByUserId,
                                  JsonObject object, String userId) {
        Map<String, CourseStatus> targetStatuses = statusesByUserId.computeIfAbsent(
                userId, key -> new LinkedHashMap<>());
        
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString()) {
                continue;
            }

            String normalizedCourseCode = CourseStatusNormalizer.normalizeCourseCode(entry.getKey());
            try {
                CourseStatus status = CourseStatus.fromInput(entry.getValue().getAsString());
                if (status != CourseStatus.Remaining) {
                    targetStatuses.put(normalizedCourseCode, status);
                }
            } catch (IllegalArgumentException ignored) {
                // Skip unknown status values to preserve forward compatibility
            }
        }
    }
}
