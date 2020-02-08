package ee.taltech.arete_admin_panel.pojo.abi.users.slug;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.FullStudentDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmptySlugDto {

    String slug;

    FullStudentDto studentDataSlug;

}
