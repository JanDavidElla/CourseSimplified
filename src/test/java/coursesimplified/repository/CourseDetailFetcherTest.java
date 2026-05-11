package coursesimplified.repository;

import coursesimplified.api.CourseApiClient;
import coursesimplified.api.dto.CourseDetailDto;
import coursesimplified.api.dto.CourseTreeResponseDto;
import coursesimplified.api.dto.ProgramDto;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CourseDetailFetcherTest {

    @Test
    public void stripsCourseCodeWhenApiUsesNonBreakingSpacesAroundDash() {
        CourseApiClient client = new CourseApiClient() {
            @Override
            public List<ProgramDto> fetchPrograms() {
                throw new UnsupportedOperationException();
            }

            @Override
            public CourseTreeResponseDto fetchCourseTree(String poid) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CourseDetailDto fetchCourseDetail(String courseCode) {
                return new CourseDetailDto("CS 46A\u00A0-\u00A0Introduction to Programming", "", "4");
            }
        };

        CourseDetailFetcher fetcher = new CourseDetailFetcher(client);
        CourseDetailFetcher.CourseDetailInfo details = fetcher.fetchDetails("CS 46A");

        assertEquals("Introduction to Programming", details.courseName());
        assertEquals(4, details.units());
    }
}
