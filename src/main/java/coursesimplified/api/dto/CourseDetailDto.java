package coursesimplified.api.dto;

import com.google.gson.annotations.SerializedName;

public record CourseDetailDto(
    @SerializedName("course_name") String courseName,
    String description,
    String units
) {}
