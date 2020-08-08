package ee.taltech.arete_admin_panel.pojo.abi.users.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationDto {

	private String email;

    private String username;

    private String password;

}
