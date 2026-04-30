package coursesimplified.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record CourseTreeResponseDto(
    List<NodeWrapper> nodes,
    List<EdgeWrapper> edges,
    @SerializedName("program_name") String programName
) {
    // The API wraps each node/edge in a "data" envelope: { "data": { ... } }
    public record NodeWrapper(CourseNodeDto data) {}
    public record EdgeWrapper(CourseEdgeDto data) {}
}
