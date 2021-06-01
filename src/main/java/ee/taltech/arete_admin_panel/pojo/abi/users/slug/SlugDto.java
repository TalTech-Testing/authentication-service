package ee.taltech.arete_admin_panel.pojo.abi.users.slug;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlugDto {

	@Id
	long id;

	String name;

	String courseUrl;

	int totalCommits;

	int totalTestsRan;

	int totalTestsPassed;

	int totalDiagnosticErrors;

	Set<String> students;

	int commitsStyleOK;

}
