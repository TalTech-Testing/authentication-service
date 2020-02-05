package ee.taltech.arete_admin_panel.pojo.abi.users;

import ee.taltech.arete_admin_panel.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserResponseIdToken {

    private long id;
    private User.Role role;
    private String token;
    private String username;
    private String color;

}
