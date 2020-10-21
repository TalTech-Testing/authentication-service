package ee.taltech.arete_admin_panel.pojo.abi.users.user;

import ee.taltech.arete_admin_panel.domain.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseIdToken {

    private long id;

    private List<Role> roles;

    private String token;

    private String username;

    private String color;

}


