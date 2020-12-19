package ee.taltech.arete_admin_panel.service;

import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
import ee.taltech.arete_admin_panel.domain.Role;
import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.exception.UserNotFoundException;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.AuthenticationDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.FullUserDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseDTO;
import ee.taltech.arete_admin_panel.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

	private final Logger logger;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public void addSuperUser(String username, String password) {
		User savedUser = userRepository.save(new User(username, password, Role.ADMIN));

		logger.info(savedUser.getUsername() + " successfully saved into DB as admin");
	}

	public void addSuperUser(String username, String passwordHash, String salt) {
		User savedUser = userRepository.save(
				User.builder()
						.username(username)
						.passwordHash(passwordHash)
						.salt(salt)
						.roles(new ArrayList<>(Collections.singletonList(Role.ADMIN)))
						.build());

		logger.info(savedUser.getUsername() + " successfully saved into DB as admin");
	}

	public List<UserResponseDTO> getAllUsers() {
		logger.info("getting all users");
		return userRepository.findAll().stream().map(user -> UserResponseDTO.builder()
				.username(user.getUsername())
				.color(user.getColor())
				.id(user.getId())
				.roles(user.getRoles())
				.token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
				.build()).collect(Collectors.toList());
	}

	public UserResponseDTO authenticateUser(@RequestBody AuthenticationDto userDto) {
		logger.info("Authenticating user {}", userDto.getUsername());
		Optional<User> userOptional = getUser(userDto.getUsername());

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			SHA512 sha512 = new SHA512();
			String passwordHash = sha512.get_SHA_512_SecurePassword(userDto.getPassword(), user.getSalt());

			if (!user.getPasswordHash().equals(passwordHash)) {
				throw new UserWrongCredentials("Wrong login.");
			}

			return UserResponseDTO.builder()
					.username(user.getUsername())
					.color(user.getColor())
					.id(user.getId())
					.password(userDto.getPassword())
					.roles(user.getRoles())
					.token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
					.build();
		} else {
			throw new UserNotFoundException("user with username: " + userDto.getUsername() + " was not found.");
		}

	}

	public Optional<User> getUser(String username) {
		return userRepository.findByUsername(username);
	}

	public void updateUserProperties(@RequestBody UserDto userDto) {
		logger.info("Update user: {}", userDto);
		Optional<User> userOptional = getUser(userDto.getUsername());

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			if (userDto.getColor() != null) {
				user.setColor(userDto.getColor());
			}

			saveUser(user);
		}
	}

	public void saveUser(User user) {
		userRepository.saveAndFlush(user);
	}

	public AuthenticationDto deleteNonAdminUser(@RequestBody AuthenticationDto userDto) {
		logger.info("Delete user: {}", userDto);

		Optional<User> userOptional = getUser(userDto.getUsername());

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			if (!user.getRoles().contains(Role.ADMIN)) {
				removeUser(userDto.getUsername());
			} else {
				throw new InvalidParameterException("Can't delete a super user");
			}
		} else {
			throw new UserNotFoundException("user with username: " + userDto.getUsername() + " was not found.");
		}

		return userDto;
	}

	public void removeUser(String username) {
		userRepository.deleteByUsername(username);
	}

	public UserResponseDTO addUser(@RequestBody FullUserDto userDto) {
		logger.info("Add user: {}", userDto.getUsername());
		if (getUser(userDto.getUsername()).isPresent()) {
			throw new DuplicateKeyException("User with that username already present");
		} else {
			long userId = saveAnyUser(userDto);
			User user = getUser(userId);

			return UserResponseDTO.builder()
					.username(user.getUsername())
					.color(user.getColor())
					.id(user.getId())
					.password(userDto.getPassword())
					.roles(user.getRoles())
					.token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
					.build();
		}

	}

	public long saveAnyUser(FullUserDto user) {
		User savedUser = userRepository.save(new User(user.getUsername(), user.getPassword(), user.getRole()));
		logger.info(savedUser.getUsername() + " successfully saved into DB");
		return savedUser.getId();
	}

	public User getUser(long id) {
		return userRepository
				.findById(id)
				.orElseThrow(() -> new UserNotFoundException("The user with the id " + id + " couldn't be found in the database."));
	}

	public UserResponseDTO addNonAdminUser(@RequestBody AuthenticationDto userDto) {
		logger.info("Add user: {}", userDto.getUsername());

		if (getUser(userDto.getUsername()).isPresent()) {
			throw new DuplicateKeyException("User with that username already present");
		} else {
			long userId = saveNonAdminUser(userDto);
			User user = getUser(userId);

			return UserResponseDTO.builder()
					.username(user.getUsername())
					.color(user.getColor())
					.id(user.getId())
					.password(userDto.getPassword())
					.roles(user.getRoles())
					.token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
					.build();
		}
	}

	public long saveNonAdminUser(AuthenticationDto user) {
		User savedUser = userRepository.save(new User(user.getUsername(), user.getPassword()));
		logger.info(savedUser.getUsername() + " successfully saved into DB");
		return savedUser.getId();
	}

	public Authentication getAuthentication(String token) {
		User userDetails = getUser(getUsername(token)).orElseThrow(() -> new UserNotFoundException("suitable user wasn't found"));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	public String getUsername(String token) {
		return Jwts.parser().setSigningKey(jwtTokenProvider.getSecretKey()).parseClaimsJws(token).getBody().getSubject();
	}
}

