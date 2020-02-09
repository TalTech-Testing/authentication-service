package ee.taltech.arete_admin_panel.pojo.abi.users.slug;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlugTableDto {

    long id;

    String name;

    String courseUrl;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    int differentStudents;

    int commitsStyleOK;

}
