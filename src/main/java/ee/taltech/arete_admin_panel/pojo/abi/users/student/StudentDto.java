package ee.taltech.arete_admin_panel.pojo.abi.users.student;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentDto {

	@Id
    long id;

    String uniid;

    long lastTested;

    int totalCommits;

    int totalTestsRan;

    int totalTestsPassed;

    int totalDiagnosticErrors;

    Set<String> courses;

	Set<String> slugs;

    int commitsStyleOK;

    Double averageGrade;

    Double medianGrade;

}
