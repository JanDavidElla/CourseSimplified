package coursesimplified.repository;

import coursesimplified.api.CourseApiClient;
import coursesimplified.api.ApiException;
import coursesimplified.api.dto.CourseDetailDto;
import coursesimplified.api.dto.CourseEdgeDto;
import coursesimplified.api.dto.CourseNodeDto;
import coursesimplified.api.dto.CourseTreeResponseDto;
import coursesimplified.model.Course;
import coursesimplified.model.CourseGraph;
import coursesimplified.model.MajorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiCourseRepository implements CourseRepository {
    private final CourseApiClient client;

    public ApiCourseRepository(CourseApiClient client) {
        this.client = client;
    }

    @Override
    public CourseGraph loadCourseGraph(MajorType major) {
        CourseTreeResponseDto treeResponse = client.fetchCourseTree(major.getPoid());
        CourseGraph graph = new CourseGraph(major);

        // Track which course codes are prereq sources so we can populate lists later
        Map<String, List<String>> prereqCodesByTarget = new HashMap<>();
        for (CourseTreeResponseDto.EdgeWrapper edgeWrapper : treeResponse.edges()) {
            CourseEdgeDto edge = edgeWrapper.data();
            prereqCodesByTarget
                .computeIfAbsent(edge.target(), k -> new ArrayList<>())
                .add(edge.source());
        }

        // First pass: build Course objects from node + detail data
        Map<String, Course.Builder> builders = new HashMap<>();
        for (CourseTreeResponseDto.NodeWrapper nodeWrapper : treeResponse.nodes()) {
            CourseNodeDto node = nodeWrapper.data();
            String courseCode = node.id();

            String courseName = courseCode;
            int units = 0;
            try {
                CourseDetailDto detail = client.fetchCourseDetail(courseCode);
                if (detail != null) {
                    courseName = extractShortName(detail.courseName(), courseCode);
                    units = parseUnits(detail.units());
                }
            } catch (ApiException e) {
                // Detail fetch failed — use course code as name, keep units at 0
            }

            builders.put(courseCode, new Course.Builder()
                    .courseCode(courseCode)
                    .courseName(courseName)
                    .totalUnits(units)
                    .isRoot(node.isRoot())
                    .isLeaf(node.isLeaf()));
        }

        // Second pass: resolve prerequisite course codes into Course objects
        // Build courses without prereqs first so they exist in the map
        Map<String, Course> courses = new HashMap<>();
        for (Map.Entry<String, Course.Builder> entry : builders.entrySet()) {
            Course course = entry.getValue().prerequisites(new ArrayList<>()).build();
            courses.put(entry.getKey(), course);
            graph.addCourse(course);
        }

        // Populate prerequisite lists now that all Course objects exist
        for (CourseTreeResponseDto.EdgeWrapper edgeWrapper : treeResponse.edges()) {
            CourseEdgeDto edge = edgeWrapper.data();
            Course target = courses.get(edge.target());
            Course source = courses.get(edge.source());
            if (target != null && source != null) {
                target.getPrerequisites().add(source);
            }
            graph.addEdge(edge.source(), edge.target());
        }

        return graph;
    }

    private String extractShortName(String fullCourseName, String courseCode) {
        // API uses non-breaking spaces ( ) around the dash separator, not regular spaces
        String normalized = fullCourseName.replace(' ', ' ');
        int dashIndex = normalized.indexOf(" - ");
        if (dashIndex != -1) {
            return normalized.substring(dashIndex + 3).trim();
        }
        return normalized;
    }

    private int parseUnits(String units) {
        if (units == null || units.isBlank()) return 0;
        try {
            return Integer.parseInt(units.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
