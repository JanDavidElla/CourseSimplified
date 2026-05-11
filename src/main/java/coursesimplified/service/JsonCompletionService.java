package coursesimplified.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import coursesimplified.model.CourseStatus;

/**
 * Manages course completion status persistence using JSON files.
 * 
 * This service coordinates the loading and saving of course statuses,
 * delegating
 * to specialized helpers for format detection, serialization, and
 * normalization.
 * Responsibilities:
 * - Coordinate status management operations
 * - Maintain current user context
 * - Delegate to CourseStatusLoader for loading
 * - Delegate to CourseStatusSerializer for saving
 * - Use CourseStatusNormalizer for consistent normalization
 */
public class JsonCompletionService implements CompletionService {
    private final Path filePath;
    private final Gson gson;
    private final String userId;
    private final Map<String, Map<String, CourseStatus>> statusesByUserId;

    public JsonCompletionService(Path filePath, Gson gson) {
        this(filePath, gson, "cli");
    }

    public JsonCompletionService(Path filePath, Gson gson, String userId) {
        this.filePath = filePath;
        this.gson = gson;
        this.userId = normalizeUserId(userId);
        this.statusesByUserId = new LinkedHashMap<>();
        load();
    }

    @Override
    public void updateStatus(String courseCode, CourseStatus status) {
        reloadFromDisk();
        String normalizedCourseCode = normalizeCourseCode(courseCode);
        Map<String, CourseStatus> currentStatuses = currentStatuses();
        CourseStatus previousStatus = currentStatuses.get(normalizedCourseCode);

        if (status == null || status == CourseStatus.Remaining) {
            currentStatuses.remove(normalizedCourseCode);
        } else {
            currentStatuses.put(normalizedCourseCode, status);
        }

        try {
            save();
        } catch (IllegalStateException e) {
            // Rollback on save failure
            if (previousStatus == null) {
                currentStatuses.remove(normalizedCourseCode);
            } else {
                currentStatuses.put(normalizedCourseCode, previousStatus);
            }
            throw e;
        }
    }

    @Override
    public CourseStatus getStatus(String courseCode) {
        return currentStatuses().getOrDefault(normalizeCourseCode(courseCode), CourseStatus.Remaining);
    }

    @Override
    public Map<String, CourseStatus> getAllStatuses() {
        return Map.copyOf(currentStatuses());
    }

    @Override
    public Set<String> getAllCompleted() {
        return CompletionService.super.getAllCompleted();
    }

    private void load() {
        if (!Files.exists(filePath))
            return;
        try {
            String json = Files.readString(filePath);
            JsonElement root = gson.fromJson(json, JsonElement.class);
            if (root == null || root.isJsonNull()) {
                return;
            }

            if (root.isJsonArray()) {
                loadLegacyCompletedArray(currentStatuses(), root.getAsJsonArray());
                return;
            }

            if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                // Preserve compatibility with the original completed-only JSON shape.
                if (object.has("completed") && object.get("completed").isJsonArray()) {
                    loadLegacyCompletedArray(currentStatuses(), object.getAsJsonArray("completed"));
                    return;
                }

                boolean looksNestedByUser = object.entrySet().stream()
                        .anyMatch(entry -> entry.getValue() != null && entry.getValue().isJsonObject());

                if (looksNestedByUser) {
                    loadNestedStatuses(object);
                } else {
                    loadFlatStatuses(currentStatuses(), object);
                }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Warning: could not read " + filePath + ": " + e.getMessage());
        }
    }

    private void reloadFromDisk() {
        statusesByUserId.clear();
        load();
    }

    private void save() {
        try {
            // Keep the file compact by omitting Remaining courses from persisted data.
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

    private String normalizeCourseCode(String courseCode) {
        return courseCode == null ? "" : courseCode.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private String normalizeUserId(String userId) {
        return userId == null ? "" : userId.trim().toLowerCase(Locale.ROOT);
    }

    private Map<String, CourseStatus> currentStatuses() {
        return statusesByUserId.computeIfAbsent(userId, key -> new LinkedHashMap<>());
    }

    private void loadLegacyCompletedArray(Map<String, CourseStatus> targetStatuses, JsonArray completedArray) {
        for (JsonElement courseCodeElement : completedArray) {
            if (courseCodeElement.isJsonPrimitive() && courseCodeElement.getAsJsonPrimitive().isString()) {
                targetStatuses.put(normalizeCourseCode(courseCodeElement.getAsString()), CourseStatus.Completed);
            }
        }
    }

    private void loadFlatStatuses(Map<String, CourseStatus> targetStatuses, JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString()) {
                continue;
            }

            String normalizedCourseCode = normalizeCourseCode(entry.getKey());
            try {
                CourseStatus status = CourseStatus.fromInput(entry.getValue().getAsString());
                if (status != CourseStatus.Remaining) {
                    targetStatuses.put(normalizedCourseCode, status);
                }
            } catch (IllegalArgumentException ignored) {
                // Skip unknown status values to preserve forward compatibility.
            }
        }
    }

    private void loadNestedStatuses(JsonObject object) {
        for (Map.Entry<String, JsonElement> userEntry : object.entrySet()) {
            if (!userEntry.getValue().isJsonObject()) {
                continue;
            }

            Map<String, CourseStatus> targetStatuses = statusesByUserId.computeIfAbsent(
                    normalizeUserId(userEntry.getKey()),
                    key -> new LinkedHashMap<>());
            loadFlatStatuses(targetStatuses, userEntry.getValue().getAsJsonObject());
        }
    }
}
