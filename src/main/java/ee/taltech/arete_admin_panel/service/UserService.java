package ee.taltech.arete_admin_panel.service;

import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.exception.UserNotFoundException;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserPostDto;
import ee.taltech.arete_admin_panel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
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

	public long addUser(UserPostDto user) {
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

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public void saveUser(User user) {
		userRepository.saveAndFlush(user);
	}

	public void removeUser(String username) {
		userRepository.deleteByUsername(username);
	}
}

