package coursesimplified.api.dto;

import com.google.gson.annotations.SerializedName;

public record ProgramDto(
    String poid,
    @SerializedName("program_name") String programName
) {}
