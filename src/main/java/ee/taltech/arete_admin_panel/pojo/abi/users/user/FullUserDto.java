package ee.taltech.arete_admin_panel.pojo.abi.users.user;

import ee.taltech.arete_admin_panel.domain.Role;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FullUserDto {

    private String username;

    private String password;

    private Role role;

}
