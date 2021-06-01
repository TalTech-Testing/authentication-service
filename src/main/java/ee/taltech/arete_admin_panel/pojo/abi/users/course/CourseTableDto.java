package ee.taltech.arete_admin_panel.pojo.abi.users.course;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "course")
public class CourseTableDto {

	@Id
	private Long id;

    String name;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    int differentStudents;

    int commitsStyleOK;

}
