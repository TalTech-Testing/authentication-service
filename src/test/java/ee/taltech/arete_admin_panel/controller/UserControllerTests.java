package ee.taltech.arete_admin_panel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.AreteAdminPanelApplication;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.AuthenticationDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseIdToken;
import ee.taltech.arete_admin_panel.service.UserService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = AreteAdminPanelApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class UserControllerTests {

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	@LocalServerPort
	private int port;

	@Before
	public void init() {
		RestAssured.port = port;
		RestAssured.basePath = "/services/arete/api/v2";
	}


	@Test
	public void adminCanLogIn() {

		userService.addSuperUser("username", "password");

		AuthenticationDto auth = new AuthenticationDto("username", "password");

		UserResponseIdToken token = getUserToken(auth);

		assert token.getUsername().equals("username");
		assert token.getToken() != null;
	}

	@Test
	public void userCanLogIn() {

		AuthenticationDto auth = new AuthenticationDto("username", "password");
		userService.addNonAdminUser(auth);

		UserResponseIdToken token = getUserToken(auth);

		assert token.getUsername().equals("username");
		assert token.getToken() != null;
	}

	@Test
	public void userCantAddNewUsers() {

		AuthenticationDto auth = new AuthenticationDto("username", "password");
		userService.addNonAdminUser(auth);
		AuthenticationDto newUser = new AuthenticationDto("user", "password");

		UserResponseIdToken token = getUserToken(auth);

		given()
				.when()
				.body(newUser)
				.header(new Header("Authorization", token.getToken()))
				.contentType(ContentType.JSON)
				.post("/user")
				.then()
				.statusCode(is(HttpStatus.SC_FORBIDDEN));
	}

	@Test
	public void adminCanAddNewUsers() {

		AuthenticationDto auth = new AuthenticationDto("username", "password");
		userService.addSuperUser("username", "password");
		AuthenticationDto newUser = new AuthenticationDto("user", "password");

		UserResponseIdToken token = getUserToken(auth);

		given()
				.when()
				.body(newUser)
				.header(new Header("Authorization", token.getToken()))
				.contentType(ContentType.JSON)
				.post("/user")
				.then()
				.statusCode(is(HttpStatus.SC_OK));
	}

	private UserResponseIdToken getUserToken(AuthenticationDto auth) {
		return given()
				.when()
				.body(auth)
				.contentType(ContentType.JSON)
				.post("/user/auth")
				.then()
				.statusCode(is(HttpStatus.SC_OK))
				.extract()
				.body()
				.as(UserResponseIdToken.class);
	}

}
