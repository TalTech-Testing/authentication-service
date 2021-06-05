package ee.taltech.arete_admin_panel.pojo.abi.users.student;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "student")
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentTableDto {

	@Id
    long id;

    String uniid;

    long lastTested;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    int differentSlugs;

    int differentCourses;

    int commitsStyleOK;

    Double averageGrade;

    Double medianGrade;

}
