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
 * Transforms course data from API DTOs into a CourseGraph domain model.
 * 
 * This builder encapsulates the complex logic of:
 * - Creating Course objects from nodes with detailed information
 * - Establishing prerequisite relationships between courses
 * - Building the complete course graph structure
 */
public class CourseGraphBuilder {
    private final CourseDetailFetcher detailFetcher;

    public CourseGraphBuilder(CourseDetailFetcher detailFetcher) {
        this.detailFetcher = detailFetcher;
    }

    /**
     * Build a CourseGraph from API response data.
     * 
     * Process:
     * 1. Fetch detailed info for each course from the API
     * 2. Create Course objects with gathered information
     * 3. Establish prerequisite relationships
     * 4. Build the complete graph
     * 
     * @param major the major type this graph represents
     * @param treeResponse the API response containing nodes and edges
     * @return a fully populated CourseGraph
     */
    public CourseGraph buildGraph(MajorType major, CourseTreeResponseDto treeResponse) {
        CourseGraph graph = new CourseGraph(major);

        // Map edge source → targets to know which courses are prerequisites
        Map<String, List<String>> prereqCodesByTarget = buildEdgeMap(treeResponse.edges());

        // Phase 1: Create Course objects with detailed information
        Map<String, Course> courses = createCourses(treeResponse.nodes(), prereqCodesByTarget);

        // Phase 2: Add courses to graph and establish relationships
        for (Course course : courses.values()) {
            graph.addCourse(course);
        }

        // Phase 3: Register edges in the graph
        for (CourseTreeResponseDto.EdgeWrapper edgeWrapper : treeResponse.edges()) {
            CourseEdgeDto edge = edgeWrapper.data();
            graph.addEdge(edge.source(), edge.target());
        }

        return graph;
    }

    /**
     * Build a mapping of target course code → list of prerequisite source codes.
     * Used to pre-compute prerequisites before creating Course objects.
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
     * Create all Course objects from node data and detailed information.
     * 
     * This uses a two-pass approach:
     * 1. Build all Course objects (with empty prerequisites initially)
     * 2. Link prerequisites now that all Course objects exist
     */
    private Map<String, Course> createCourses(List<CourseTreeResponseDto.NodeWrapper> nodes,
                                               Map<String, List<String>> prereqCodesByTarget) {
        // First pass: Create all Course objects
        Map<String, Course> courses = new HashMap<>();
        
        for (CourseTreeResponseDto.NodeWrapper nodeWrapper : nodes) {
            CourseNodeDto node = nodeWrapper.data();
            String courseCode = node.id();
            
            // Fetch detailed information from API
            CourseDetailFetcher.CourseDetailInfo detailInfo = detailFetcher.fetchDetails(courseCode);
            
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

        // Second pass: Link prerequisites
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
