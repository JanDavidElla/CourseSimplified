package coursesimplified.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import coursesimplified.model.CourseStatus;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonCompletionServiceTest {
    private final Gson gson = new GsonBuilder().create();

    @Test
    public void loadsLegacyCompletedJsonWithoutBreakingExistingData() throws IOException {
        Path tempFile = Files.createTempFile("coursesimplified-legacy-statuses", ".json");
        try {
            Files.writeString(tempFile, "{\"completed\":[\"CS 46A\",\"MATH 42\"]}");

            JsonCompletionService service = new JsonCompletionService(tempFile, gson);

            assertEquals(CourseStatus.Completed, service.getStatus("CS 46A"));
            assertEquals(CourseStatus.Completed, service.getStatus("MATH 42"));
            assertEquals(CourseStatus.Remaining, service.getStatus("CS 146"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void persistsThreeStateStatusesAsCourseStatusMap() throws IOException {
        Path tempFile = Files.createTempFile("coursesimplified-statuses", ".json");
        try {
            JsonCompletionService service = new JsonCompletionService(tempFile, gson);

            service.updateStatus("CS 46A", CourseStatus.Completed);
            service.updateStatus("MATH 42", CourseStatus.InProgress);
            service.updateStatus("CS 46A", CourseStatus.Remaining);

            String json = Files.readString(tempFile);

            assertFalse(json.contains("\"CS 46A\""));
            assertTrue(json.contains("\"MATH 42\":\"InProgress\""));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
