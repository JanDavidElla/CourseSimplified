package coursesimplified.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import coursesimplified.model.CourseStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JsonCompletionService implements CompletionService {
    private final Path filePath;
    private final Gson gson;
    private final Map<String, CourseStatus> statusesByCode;

    public JsonCompletionService(Path filePath, Gson gson) {
        this.filePath = filePath;
        this.gson = gson;
        this.statusesByCode = new LinkedHashMap<>();
        load();
    }

    @Override
    public void updateStatus(String courseCode, CourseStatus status) {
        String normalizedCourseCode = normalizeCourseCode(courseCode);
        CourseStatus previousStatus = statusesByCode.get(normalizedCourseCode);

        if (status == null || status == CourseStatus.Remaining) {
            statusesByCode.remove(normalizedCourseCode);
        } else {
            statusesByCode.put(normalizedCourseCode, status);
        }

        try {
            save();
        } catch (IllegalStateException e) {
            if (previousStatus == null) {
                statusesByCode.remove(normalizedCourseCode);
            } else {
                statusesByCode.put(normalizedCourseCode, previousStatus);
            }
            throw e;
        }
    }

    @Override
    public CourseStatus getStatus(String courseCode) {
        return statusesByCode.getOrDefault(normalizeCourseCode(courseCode), CourseStatus.Remaining);
    }

    @Override
    public Map<String, CourseStatus> getAllStatuses() {
        return Map.copyOf(statusesByCode);
    }

    @Override
    public Set<String> getAllCompleted() {
        return CompletionService.super.getAllCompleted();
    }

    private void load() {
        if (!Files.exists(filePath)) return;
        try {
            String json = Files.readString(filePath);
            JsonElement root = gson.fromJson(json, JsonElement.class);
            if (root == null || root.isJsonNull()) {
                return;
            }

            if (root.isJsonArray()) {
                loadLegacyCompletedArray(root.getAsJsonArray());
                return;
            }

            if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                // Preserve compatibility with the original completed-only JSON shape.
                if (object.has("completed") && object.get("completed").isJsonArray()) {
                    loadLegacyCompletedArray(object.getAsJsonArray("completed"));
                    return;
                }

                // Current format stores only non-remaining statuses as courseCode -> status.
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString()) {
                        continue;
                    }

                    String normalizedCourseCode = normalizeCourseCode(entry.getKey());
                    try {
                        CourseStatus status = CourseStatus.fromInput(entry.getValue().getAsString());
                        if (status != CourseStatus.Remaining) {
                            statusesByCode.put(normalizedCourseCode, status);
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Skip unknown status values to preserve forward compatibility.
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Warning: could not read " + filePath + ": " + e.getMessage());
        }
    }

    private void save() {
        try {
            // Keep the file compact by omitting Remaining courses from persisted data.
            Map<String, String> serializedStatuses = new TreeMap<>();
            for (Map.Entry<String, CourseStatus> entry : statusesByCode.entrySet()) {
                serializedStatuses.put(entry.getKey(), entry.getValue().name());
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

    private void loadLegacyCompletedArray(JsonArray completedArray) {
        for (JsonElement courseCodeElement : completedArray) {
            if (courseCodeElement.isJsonPrimitive() && courseCodeElement.getAsJsonPrimitive().isString()) {
                statusesByCode.put(normalizeCourseCode(courseCodeElement.getAsString()), CourseStatus.Completed);
            }
        }
    }
}
