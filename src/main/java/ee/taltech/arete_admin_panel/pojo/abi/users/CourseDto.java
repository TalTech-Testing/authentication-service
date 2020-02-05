package ee.taltech.arete_admin_panel.pojo.abi.users;

import ee.taltech.arete_admin_panel.domain.Slug;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    Set<SlugDto> slugs = new HashSet<>();

    // TODO sum all up

}
