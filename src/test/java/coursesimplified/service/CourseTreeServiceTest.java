package coursesimplified.service;

import coursesimplified.model.Course;
import coursesimplified.model.CourseGraph;
import coursesimplified.model.CourseStatus;
import coursesimplified.model.Major;
import coursesimplified.model.MajorType;
import coursesimplified.repository.CourseRepository;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CourseTreeServiceTest {

    @Test
    public void loadsComputerScienceRoadmap() {
        CourseTreeService service = createService(Map.of());

        Major major = service.loadMajor(MajorType.CS);

        assertEquals(MajorType.CS, major.getType());
        assertEquals("Computer Science, BS", major.getMajorName());
        assertEquals(3, major.getCourseGraph().getTotalCount());
        assertEquals(1, major.getCourseGraph().getRootCourses().size());
        assertEquals("CS 46A", major.getCourseGraph().getRootCourses().get(0).getCourseCode());
    }

    @Test
    public void loadsSoftwareEngineeringRoadmap() {
        CourseTreeService service = createService(Map.of());

        Major major = service.loadMajor(MajorType.SWE);

        assertEquals(MajorType.SWE, major.getType());
        assertEquals("Software Engineering, BS", major.getMajorName());
        assertEquals(3, major.getCourseGraph().getTotalCount());
        assertEquals(1, major.getCourseGraph().getRootCourses().size());
        assertEquals("ENGR 10", major.getCourseGraph().getRootCourses().get(0).getCourseCode());
    }

    @Test
    public void updatesValidCourseToCompleted() {
        CourseTreeService service = createService(Map.of());
        service.loadMajor(MajorType.CS);

        String result = service.updateCourseStatus("  cs   46a ", CourseStatus.Completed);

        Course course = service.findCourseInCurrentMajor("CS 46A").orElseThrow();
        assertEquals("CS 46A updated to Completed", result);
        assertEquals(CourseStatus.Completed, course.getStatus());
        assertTrue(course.isCompleted());
    }

    @Test
    public void updatesValidCourseToInProgress() {
        CourseTreeService service = createService(Map.of());
        service.loadMajor(MajorType.CS);

        String result = service.updateCourseStatus("CS 46B", CourseStatus.InProgress);

        Course course = service.findCourseInCurrentMajor("CS 46B").orElseThrow();
        assertEquals("CS 46B updated to In Progress", result);
        assertEquals(CourseStatus.InProgress, course.getStatus());
        assertFalse(course.isCompleted());
        assertTrue(course.isInProgress());
    }

    @Test
    public void revertsCourseToRemaining() {
        CourseTreeService service = createService(Map.of("CS 46A", CourseStatus.Completed));
        service.loadMajor(MajorType.CS);

        String result = service.updateCourseStatus("CS 46A", CourseStatus.Remaining);

        Course course = service.findCourseInCurrentMajor("CS 46A").orElseThrow();
        assertEquals("CS 46A updated to Remaining", result);
        assertEquals(CourseStatus.Remaining, course.getStatus());
        assertFalse(course.isCompleted());
        assertFalse(course.isInProgress());
    }

    @Test
    public void rejectsInvalidCourseIdsWithoutChangingProgress() {
        CourseTreeService service = createService(Map.of());
        service.loadMajor(MajorType.CS);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCourseStatus("MATH 42", CourseStatus.Completed)
        );

        assertEquals("Course 'MATH 42' not found in current major.", error.getMessage());
        assertTrue(service.findCourseInCurrentMajor("MATH 42").isEmpty());
        assertEquals(0, service.getCurrentMajor().getCourseGraph().getCompletedCount());
        assertEquals(0, service.getCurrentMajor().getCourseGraph().getInProgressCount());
    }

    @Test
    public void progressCountsOnlyCompletedCourses() {
        CourseTreeService service = createService(Map.of("CS 46A", CourseStatus.Completed));
        Major major = service.loadMajor(MajorType.CS);

        service.updateCourseStatus("CS 46B", CourseStatus.InProgress);

        assertEquals(1, major.getCourseGraph().getCompletedCount());
        assertEquals(1, major.getCourseGraph().getInProgressCount());
        assertEquals(1, service.getCompletedCourseCount());
        assertEquals(1, service.getInProgressCourseCount());
        assertEquals(1, service.getRemainingCourseCount());
        assertEquals(3, service.getTotalCourseCount());
    }

    @Test
    public void eligibilityRequiresPrerequisitesToBeCompleted() {
        CourseTreeService service = createService(Map.of());
        service.loadMajor(MajorType.CS);

        Course advancedCourse = service.findCourseInCurrentMajor("CS 146").orElseThrow();
        assertEquals(1, advancedCourse.getPrerequisites().size());
        assertFalse(allPrerequisitesCompleted(advancedCourse));

        service.updateCourseStatus("CS 46A", CourseStatus.Completed);
        service.updateCourseStatus("CS 46B", CourseStatus.InProgress);
        assertFalse(allPrerequisitesCompleted(advancedCourse));

        service.updateCourseStatus("CS 46B", CourseStatus.Completed);
        assertTrue(allPrerequisitesCompleted(advancedCourse));
    }

    private CourseTreeService createService(Map<String, CourseStatus> initialStatuses) {
        return new CourseTreeService(new StubCourseRepository(), new InMemoryCompletionService(initialStatuses));
    }

    private boolean allPrerequisitesCompleted(Course course) {
        return course.getPrerequisites().stream().allMatch(Course::isCompleted);
    }

    private static final class StubCourseRepository implements CourseRepository {
        @Override
        public CourseGraph loadCourseGraph(MajorType major) {
            return switch (major) {
                case CS -> createCsGraph();
                case SWE -> createSweGraph();
            };
        }

        private CourseGraph createCsGraph() {
            CourseGraph graph = new CourseGraph(MajorType.CS);

            Course cs46a = course("CS 46A", "Introduction to Programming", 4, true, false);
            Course cs46b = course("CS 46B", "Data Structures", 4, false, false);
            Course cs146 = course("CS 146", "Data Structures and Algorithms", 3, false, true);

            cs46b.getPrerequisites().add(cs46a);
            cs146.getPrerequisites().add(cs46b);

            addCourses(graph, cs46a, cs46b, cs146);
            graph.addEdge("CS 46A", "CS 46B");
            graph.addEdge("CS 46B", "CS 146");
            return graph;
        }

        private CourseGraph createSweGraph() {
            CourseGraph graph = new CourseGraph(MajorType.SWE);

            Course engr10 = course("ENGR 10", "Introduction to Engineering", 1, true, false);
            Course cmpe30 = course("CMPE 30", "Object-Oriented Programming", 3, false, false);
            Course cmpe131 = course("CMPE 131", "Software Engineering I", 3, false, true);

            cmpe30.getPrerequisites().add(engr10);
            cmpe131.getPrerequisites().add(cmpe30);

            addCourses(graph, engr10, cmpe30, cmpe131);
            graph.addEdge("ENGR 10", "CMPE 30");
            graph.addEdge("CMPE 30", "CMPE 131");
            return graph;
        }

        private Course course(String code, String name, int units, boolean isRoot, boolean isLeaf) {
            return new Course.Builder()
                    .courseCode(code)
                    .courseName(name)
                    .totalUnits(units)
                    .isRoot(isRoot)
                    .isLeaf(isLeaf)
                    .build();
        }

        private void addCourses(CourseGraph graph, Course... courses) {
            for (Course course : courses) {
                graph.addCourse(course);
            }
        }
    }

    private static final class InMemoryCompletionService implements CompletionService {
        private final Map<String, CourseStatus> statusesByCode = new HashMap<>();

        private InMemoryCompletionService(Map<String, CourseStatus> initialStatuses) {
            for (Map.Entry<String, CourseStatus> entry : initialStatuses.entrySet()) {
                updateStatus(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void updateStatus(String courseCode, CourseStatus status) {
            String normalizedCourseCode = normalize(courseCode);
            if (status == null || status == CourseStatus.Remaining) {
                statusesByCode.remove(normalizedCourseCode);
            } else {
                statusesByCode.put(normalizedCourseCode, status);
            }
        }

        @Override
        public CourseStatus getStatus(String courseCode) {
            return statusesByCode.getOrDefault(normalize(courseCode), CourseStatus.Remaining);
        }

        @Override
        public Map<String, CourseStatus> getAllStatuses() {
            return Map.copyOf(statusesByCode);
        }

        private String normalize(String courseCode) {
            return courseCode == null ? "" : courseCode.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        }
    }
}
