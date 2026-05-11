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
 * Loads course statuses from a JSON file.
 * 
 * Supports 4 old JSON formats (for backward compatibility):
 * - [\"CS101\", \"CS102\"]
 * - {\"completed\": [\"CS101\", \"CS102\"]}
 * - {\"user1\": {\"CS101\": \"Completed\"}, ...}
 * - {\"CS101\": \"Completed\", ...}
 */
public class CourseStatusLoader {
    private final Gson gson;

    public CourseStatusLoader(Gson gson) {
        this.gson = gson;
    }

    /**
     * Load from JSON file. Auto-detects old formats (for backward compat).
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
            // FORMAT 1: Legacy array format - ["CS101", "CS102"]
            // All courses treated as Completed, stored under default user "cli"
            loadLegacyCompletedArray(statusesByUserId, root.getAsJsonArray());
            return;
        }

        if (root.isJsonObject()) {
            JsonObject object = root.getAsJsonObject();
            
            // FORMAT 2: Legacy object with "completed" field
            // Example: {"completed": ["CS101", "CS102"]}
            // All courses treated as Completed, stored under default user "cli"
            if (object.has("completed") && object.get("completed").isJsonArray()) {
                loadLegacyCompletedArray(statusesByUserId, object.getAsJsonArray("completed"));
                return;
            }

            // Distinguish between nested-by-user vs flat format by checking if
            // any values are objects (nested) vs strings (flat)
            boolean looksNestedByUser = object.entrySet().stream()
                    .anyMatch(entry -> entry.getValue() != null && entry.getValue().isJsonObject());

            if (looksNestedByUser) {
                // FORMAT 3: Nested by user - {"user1": {...}, "user2": {...}}
                // Different users with their own course status maps
                loadNestedStatuses(object, statusesByUserId);
            } else {
                // FORMAT 4: Flat format - {"CS101": "Completed", "CS102": "InProgress"}
                // Single user (defaults to "cli") with multiple courses
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
