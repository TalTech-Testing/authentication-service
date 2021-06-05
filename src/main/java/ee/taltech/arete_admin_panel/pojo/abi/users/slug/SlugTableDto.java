package ee.taltech.arete_admin_panel.pojo.abi.users.slug;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "slug")
public class SlugTableDto {

	@Id
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
