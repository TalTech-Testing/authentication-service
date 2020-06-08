package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserPostDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.stream.Collectors;

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "user", description = "User authentication")
@RestController()
@RequestMapping("services/arete/api/admin")
public class UserController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication

	public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
		this.authenticationManager = authenticationManager;
	}


	@Operation(summary = "Generates a JWT upon authenticating user input", tags = {"user"})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(path = "/auth")
	public UserResponseIdToken getHome(@RequestBody UserPostDto userDto) throws AuthenticationException {
		try {
			return authenticateUser(userDto);

		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Updates users' settings", tags = {"user"})
	@ResponseStatus(HttpStatus.OK)
	@PutMapping(path = "/user")
	public void setUserProperties(@RequestBody UserDto userDto) throws AuthenticationException {
		try {
			updateUserProperties(userDto);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	public UserResponseIdToken authenticateUser(@RequestBody UserPostDto userDto) {
		LOG.info("Authenticating user {}", userDto.getUsername());
		User user = userService.getUser(userDto.getUsername());

		SHA512 sha512 = new SHA512();
		String passwordHash = sha512.get_SHA_512_SecurePassword(userDto.getPassword(), user.getSalt());

		if (!user.getPasswordHash().equals(passwordHash)) {
			throw new UserWrongCredentials("Wrong login.");
		}

		return UserResponseIdToken.builder()
				.username(user.getUsername())
				.color(user.getColor())
				.id(user.getId())
				.roles(user.getRoles())
				.token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
				.build();
	}

	public void updateUserProperties(@RequestBody UserDto userDto) {
		User user = userService.getUser(userDto.getUsername());

		if (userDto.getColor() != null) {
			user.setColor(userDto.getColor());
		}

		userService.saveUser(user);

		LOG.info("Successfully updated {}", user.getUsername());
	}
}
