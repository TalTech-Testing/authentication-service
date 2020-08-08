package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.pojo.abi.users.user.AuthenticationDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseIdToken;
import ee.taltech.arete_admin_panel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.List;

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "user", description = "User authentication")
@RestController()
@RequestMapping("services/arete/api/v2/user")
public class UserController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final UserService userService;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication

	@Autowired
	public UserController(UserService userService, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
	}


	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Gets all users", tags = {"user"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/all")
	public List<UserResponseIdToken> getAllUsers() throws AuthenticationException {
		try {
			return userService.getAllUsers();

		} catch (Exception e) {
			throw new AuthenticationException(e.getMessage());
		}
	}

	@Operation(summary = "Generates a JWT upon authenticating user input", tags = {"user"})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(path = "/auth")
	public UserResponseIdToken authenticate(@RequestBody AuthenticationDto userDto) throws AuthenticationException {
		try {
			return userService.authenticateUser(userDto);

		} catch (Exception e) {
			throw new AuthenticationException(e.getMessage());
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Adds a new non admin user to database", tags = {"user"})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(path = "")
	public UserResponseIdToken addUser(@RequestBody AuthenticationDto userDto) throws AuthenticationException {
		try {
			return userService.addNonAdminUser(userDto);

		} catch (Exception e) {
			throw new AuthenticationException(e.getMessage());
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Deletes a non admin user from database", tags = {"user"})
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping(path = "")
	public AuthenticationDto deleteUser(@RequestBody AuthenticationDto userDto) throws AuthenticationException {
		try {

			return userService.deleteNonAdminUser(userDto);

		} catch (Exception e) {
			throw new AuthenticationException(e.getMessage());
		}
	}


	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Updates users' settings", tags = {"user"})
	@ResponseStatus(HttpStatus.OK)
	@PutMapping(path = "")
	public void setUserProperties(@RequestBody UserDto userDto) throws AuthenticationException {
		try {
			userService.updateUserProperties(userDto);
		} catch (Exception e) {
			throw new AuthenticationException(e.getMessage());
		}
	}
}
