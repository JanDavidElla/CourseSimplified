package coursesimplified.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonCompletionService implements CompletionService {
    private final Path filePath;
    private final Gson gson;
    private final Set<String> completedCodes;

    public JsonCompletionService(Path filePath, Gson gson) {
        this.filePath = filePath;
        this.gson = gson;
        this.completedCodes = new HashSet<>();
        load();
    }

    @Override
    public void markCompleted(String courseCode) {
        completedCodes.add(courseCode.trim());
        save();
    }

    @Override
    public void markIncomplete(String courseCode) {
        completedCodes.remove(courseCode.trim());
        save();
    }

    @Override
    public boolean isCompleted(String courseCode) {
        return completedCodes.contains(courseCode.trim());
    }

    @Override
    public Set<String> getAllCompleted() {
        return Set.copyOf(completedCodes);
    }

    private void load() {
        if (!Files.exists(filePath)) return;
        try {
            String json = Files.readString(filePath);
            CompletionData data = gson.fromJson(json, CompletionData.class);
            if (data != null && data.completed() != null) {
                completedCodes.addAll(data.completed());
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read " + filePath + ": " + e.getMessage());
        }
    }

    private void save() {
        try {
            String json = gson.toJson(new CompletionData(List.copyOf(completedCodes)));
            Files.writeString(filePath, json);
        } catch (IOException e) {
            System.err.println("Warning: could not save " + filePath + ": " + e.getMessage());
        }
    }

    private record CompletionData(List<String> completed) {}
}
