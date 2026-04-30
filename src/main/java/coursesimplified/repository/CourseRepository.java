package coursesimplified.repository;

import coursesimplified.model.CourseGraph;
import coursesimplified.model.MajorType;

public interface CourseRepository {
    CourseGraph loadCourseGraph(MajorType major);
}
