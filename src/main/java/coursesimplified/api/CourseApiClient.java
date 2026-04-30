package coursesimplified.api;

import coursesimplified.api.dto.CourseDetailDto;
import coursesimplified.api.dto.CourseTreeResponseDto;
import coursesimplified.api.dto.ProgramDto;

import java.util.List;

public interface CourseApiClient {
    List<ProgramDto> fetchPrograms();
    CourseTreeResponseDto fetchCourseTree(String poid);
    CourseDetailDto fetchCourseDetail(String courseCode);
}
