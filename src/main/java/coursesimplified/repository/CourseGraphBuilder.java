package coursesimplified.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coursesimplified.api.dto.CourseEdgeDto;
import coursesimplified.api.dto.CourseNodeDto;
import coursesimplified.api.dto.CourseTreeResponseDto;
import coursesimplified.model.Course;
import coursesimplified.model.CourseGraph;
import coursesimplified.model.MajorType;

/**
 * Builds a graph of courses from API data.
 * 
 * Creates all course objects first, then links them together.
 * Keeps this separate from API stuff.
 */
public class CourseGraphBuilder {
    private final CourseDetailFetcher detailFetcher;

    public CourseGraphBuilder(CourseDetailFetcher detailFetcher) {
        this.detailFetcher = detailFetcher;
    }

    /**
     * Build graph in 2 steps: create courses first, then link them.
     */
    public CourseGraph buildGraph(MajorType major, CourseTreeResponseDto treeResponse) {
        CourseGraph graph = new CourseGraph(major);

        // Figure out which courses need which prerequisites
        Map<String, List<String>> prereqCodesByTarget = buildEdgeMap(treeResponse.edges());

        // Create all course objects
        Map<String, Course> courses = createCourses(treeResponse.nodes(), prereqCodesByTarget);

        // Add them to the graph
        for (Course course : courses.values()) {
            graph.addCourse(course);
        }

        // Add the prerequisite relationships
        for (CourseTreeResponseDto.EdgeWrapper edgeWrapper : treeResponse.edges()) {
            CourseEdgeDto edge = edgeWrapper.data();
            graph.addEdge(edge.source(), edge.target());
        }

        return graph;
    }

    /**
     * Pre-compute which courses need which prerequisites.
     * Makes it faster to look up later.
     */
    private Map<String, List<String>> buildEdgeMap(List<CourseTreeResponseDto.EdgeWrapper> edges) {
        Map<String, List<String>> prereqCodesByTarget = new HashMap<>();
        
        for (CourseTreeResponseDto.EdgeWrapper edgeWrapper : edges) {
            CourseEdgeDto edge = edgeWrapper.data();
            prereqCodesByTarget
                .computeIfAbsent(edge.target(), k -> new ArrayList<>())
                .add(edge.source());
        }
        
        return prereqCodesByTarget;
    }

    /**
     * Create all courses then link them together.
     * Can't link them if they don't exist yet, so gotta do it in 2 steps.
     */
    private Map<String, Course> createCourses(List<CourseTreeResponseDto.NodeWrapper> nodes,
                                               Map<String, List<String>> prereqCodesByTarget) {
        // Step 1: Create all courses
        Map<String, Course> courses = new HashMap<>();
        
        for (CourseTreeResponseDto.NodeWrapper nodeWrapper : nodes) {
            CourseNodeDto node = nodeWrapper.data();
            String courseCode = node.id();
            
            // Get course info from API
            CourseDetailFetcher.CourseDetailInfo detailInfo = detailFetcher.fetchDetails(courseCode);
            
            // Build course (no prerequisites yet)
            Course course = new Course.Builder()
                    .courseCode(courseCode)
                    .courseName(detailInfo.courseName())
                    .totalUnits(detailInfo.units())
                    .isRoot(node.isRoot())
                    .isLeaf(node.isLeaf())
                    .prerequisites(new ArrayList<>())
                    .build();
            
            courses.put(courseCode, course);
        }

        // Step 2: Link prerequisites together
        for (Map.Entry<String, List<String>> entry : prereqCodesByTarget.entrySet()) {
            String targetCode = entry.getKey();
            List<String> sourceCodes = entry.getValue();
            
            Course target = courses.get(targetCode);
            if (target != null) {
                for (String sourceCode : sourceCodes) {
                    Course source = courses.get(sourceCode);
                    if (source != null) {
                        target.addPrerequisite(source);
                    }
                }
            }
        }

        return courses;
    }
}
