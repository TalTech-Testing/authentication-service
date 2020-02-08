package ee.taltech.arete_admin_panel.pojo.abi.users.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private String username;

    private String token;

    private String color;

}
