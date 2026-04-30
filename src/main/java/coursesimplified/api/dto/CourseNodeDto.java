package coursesimplified.api.dto;

import com.google.gson.annotations.SerializedName;

public record CourseNodeDto(
    String id,
    String label,
    String department,
    @SerializedName("is_root") boolean isRoot,
    @SerializedName("is_leaf") boolean isLeaf
) {}
