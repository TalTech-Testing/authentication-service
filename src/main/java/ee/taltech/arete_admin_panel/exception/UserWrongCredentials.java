package ee.taltech.arete_admin_panel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserWrongCredentials extends RuntimeException {
	public UserWrongCredentials() {
		super();
	}

	public UserWrongCredentials(String message) {
		super(message);
	}

	public UserWrongCredentials(String message, Throwable cause) {
		super(message, cause);
	}
}
