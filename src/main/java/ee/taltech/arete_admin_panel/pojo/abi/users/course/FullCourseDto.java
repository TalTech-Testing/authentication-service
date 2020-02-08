package ee.taltech.arete_admin_panel.pojo.abi.users.course;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ee.taltech.arete_admin_panel.domain.CodeError;
import ee.taltech.arete_admin_panel.domain.CourseStudent;
import ee.taltech.arete_admin_panel.domain.SlugStudent;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugDto;
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

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    Set<CodeError> diagnosticCodeErrors;

    Integer totalTestErrors;

    Set<CodeError> testCodeErrors;

    int failedCommits;

    int commitsStyleOK;

}
