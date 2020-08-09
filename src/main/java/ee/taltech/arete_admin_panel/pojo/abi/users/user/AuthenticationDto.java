package ee.taltech.arete_admin_panel.pojo.abi.users.user;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationDto {

	private String email;

    private String username;

    private String password;

}
