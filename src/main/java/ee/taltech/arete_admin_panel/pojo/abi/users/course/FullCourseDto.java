package ee.taltech.arete_admin_panel.pojo.abi.users.course;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ee.taltech.arete_admin_panel.domain.CodeError;
import ee.taltech.arete_admin_panel.domain.CourseStudent;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FullCourseDto {

    long id;

    String gitUrl;

    String name;

    Set<CourseStudent> students;

    Set<CodeError> diagnosticCodeErrors;

    Set<CodeError> testCodeErrors;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    Integer totalTestErrors;

    int failedCommits;

    int commitsStyleOK;

}
