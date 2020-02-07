package ee.taltech.arete_admin_panel.pojo.abi.users.user;

import ee.taltech.arete_admin_panel.domain.User;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseIdToken {

    private long id;

    private User.Role role;

    private String token;

    private String username;

    private String color;

}


