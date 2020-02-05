package ee.taltech.arete_admin_panel.pojo.abi.users;

import ee.taltech.arete_admin_panel.domain.StudentDataSlug;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlugDto {

    String slug;
    StudentDataSlug studentDataSlug;

}
