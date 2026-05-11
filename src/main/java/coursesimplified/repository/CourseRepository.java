package coursesimplified.repository;

import coursesimplified.model.CourseGraph;
import coursesimplified.model.MajorType;

/**
 * Interface for loading course graphs from somewhere (API, database, file, etc).
 */
public interface CourseRepository {
    CourseGraph loadCourseGraph(MajorType major);
}
