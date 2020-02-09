package ee.taltech.arete_admin_panel.pojo.abi.users.course;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseTableDto {

    long id;

    String name;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    int differentStudents;

    int commitsStyleOK;

}
