package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Files")
@Entity
@JsonClassDescription("File class")
public class File {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JsonPropertyDescription("Path for the file")
	private String path;

	@JsonPropertyDescription("File content")
	@Column(columnDefinition = "TEXT")
	private String contents;

}
