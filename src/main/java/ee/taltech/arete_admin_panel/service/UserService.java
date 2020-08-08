package ee.taltech.arete_admin_panel.service;

import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.exception.UserNotFoundException;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.AuthenticationDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseIdToken;
import ee.taltech.arete_admin_panel.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Autowired
	public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	public void addSuperUser(String username, String password) {
		User savedUser = userRepository.save(new User(username, password, User.Role.ADMIN));

		LOG.info(savedUser.getUsername() + " successfully saved into DB as admin");
	}

	public void addSuperUser(String username, String passwordHash, String salt) {
		User savedUser = userRepository.save(
				User.builder()
						.username(username)
						.passwordHash(passwordHash)
						.salt(salt)
						.roles(new ArrayList<>(Collections.singletonList(User.Role.ADMIN)))
						.build());

		LOG.info(savedUser.getUsername() + " successfully saved into DB as admin");
	}

	public long saveNonAdminUser(AuthenticationDto user) {
		User savedUser = userRepository.save(new User(user.getUsername(), user.getPassword()));
		LOG.info(savedUser.getUsername() + " successfully saved into DB");
		return savedUser.getId();
	}

	public User getUser(long id) {
		return userRepository
				.findById(id)
				.map(
						user -> {
							LOG.info("Reading user with id " + id + " from database.");
							return user;
						})
				.orElseThrow(
						() -> {
							LOG.error(String.format("User with id %d was not found.", id));
							return new UserNotFoundException("The user with the id " + id + " couldn't be found in the database.");
						});
	}

	public User getUser(String username) {
		return userRepository
				.findByUsername(username)
				.map(
						user -> {
							LOG.info("Reading user with username " + username + " from database.");
							return user;
						})
				.orElseThrow(
						() -> {
							LOG.error(String.format("User with username %s was not found.", username));
							return new UserNotFoundException("The user with the username: " + username + " couldn't be found in the database.");
						});
	}

	public long getHome(String username) {
		Optional<User> user = userRepository.findByUsername(username);
		if (user.isEmpty()) {
			LOG.error(String.format("User with username %s was not found.", username));
			throw new UserNotFoundException(String.format("User with username %s was not found.", username));
		}
		return user.get().getId();
	}

	public List<UserResponseIdToken> getAllUsers() {
		return userRepository.findAll().stream().map(user -> UserResponseIdToken.builder()
				.username(user.getUsername())
				.color(user.getColor())
				.id(user.getId())
				.roles(user.getRoles())
				.token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
				.build()).collect(Collectors.toList());
	}

	public void saveUser(User user) {
		userRepository.saveAndFlush(user);
	}

	public void removeUser(String username) {
		userRepository.deleteByUsername(username);
	}

	public UserResponseIdToken authenticateUser(@RequestBody AuthenticationDto userDto) {
		LOG.info("Authenticating user {}", userDto.getUsername());
		User user = getUser(userDto.getUsername());

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
		User user = getUser(userDto.getUsername());

		if (userDto.getColor() != null) {
			user.setColor(userDto.getColor());
		}

		saveUser(user);

		LOG.info("Successfully updated {}", user.getUsername());
	}

	public AuthenticationDto deleteNonAdminUser(@RequestBody AuthenticationDto userDto) throws AuthenticationException {
		LOG.info("Delete user {}", userDto.getUsername());

		if (!getUser(userDto.getUsername()).getRoles().contains(User.Role.ADMIN)) {
			removeUser(userDto.getUsername());
		} else {
			throw new AuthenticationException("Can't delete a super user");
		}

		return userDto;
	}

	public UserResponseIdToken addNonAdminUser(@RequestBody AuthenticationDto userDto) throws Exception {
		LOG.info("Add user {}", userDto.getUsername());

		try {
			getUser(userDto.getUsername());
		} catch (UserNotFoundException e) {
			long userId = saveNonAdminUser(userDto);
			User user = getUser(userId);

			return UserResponseIdToken.builder()
					.username(user.getUsername())
					.color(user.getColor())
					.id(user.getId())
					.roles(user.getRoles())
					.token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
					.build();
		}

		throw new Exception("User with that username already present");
	}

	public String getUsername(String token) {
		return Jwts.parser().setSigningKey(jwtTokenProvider.getSecretKey()).parseClaimsJws(token).getBody().getSubject();
	}

	public Authentication getAuthentication(String token) {
		User userDetails = getUser(getUsername(token));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}
}

