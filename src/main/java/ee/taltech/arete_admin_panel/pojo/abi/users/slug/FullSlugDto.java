package ee.taltech.arete_admin_panel.pojo.abi.users.slug;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ee.taltech.arete_admin_panel.domain.CodeError;
import ee.taltech.arete_admin_panel.domain.Student;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FullSlugDto {

    long id;

    String name;

    String courseUrl;

    Set<Student> students;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    Set<CodeError> diagnosticCodeErrors;

    int totalTestErrors = 0;

    int failedCommits;

    int commitsStyleOK;
}
