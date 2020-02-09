package ee.taltech.arete_admin_panel.pojo.abi.users.student;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ee.taltech.arete_admin_panel.domain.CodeError;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugTableDto;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FullStudentDto {

    long id;

    String uniid;

    Set<CodeError> diagnosticCodeErrors;

    Set<CodeError> testCodeErrors;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    int totalTestErrors;

    int failedCommits;

    int commitsStyleOK;

}
