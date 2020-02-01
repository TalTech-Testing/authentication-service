package ee.taltech.arete_admin_panel.pojo.abi.users;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserResponseIdToken {
	private long id;
	private boolean isAdmin;
	private String token;
}
