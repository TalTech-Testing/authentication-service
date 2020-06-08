package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.domain.SystemState;
import ee.taltech.arete_admin_panel.service.AreteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "state", description = "server status")
@RestController()
@RequestMapping("services/arete/api/v2/state")
public class StateController {

	private final AreteService areteService;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication


	public StateController(AreteService areteService, AuthenticationManager authenticationManager) {
		this.areteService = areteService;
		this.authenticationManager = authenticationManager;
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Return backends' state", tags = {"state"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/")
	public SystemState getState() throws AuthenticationException {
		try {
			try {
				return new SystemState();
			} catch (Exception e) {
				throw new RequestRejectedException(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}


	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Return testers' state", tags = {"state"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/tester")
	public arete.java.response.SystemState getTesterState() throws AuthenticationException {
		try {
			try {
				return areteService.getTesterState();
			} catch (Exception e) {
				throw new RequestRejectedException(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Return backends' logs", tags = {"state"}, deprecated = true)
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/logs")
	public String getLogs() throws AuthenticationException {
		try {
			try {
				return String.join("", tailFile(Paths.get("logs/spring.log"), 2000));
			} catch (IOException e) {
				return e.getMessage();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Return testers' logs", tags = {"state"}, deprecated = true)
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/logs/tester")
	public String getTesterLogs() throws AuthenticationException {
		try {
			try {
				return areteService.getTesterLogs();
			} catch (Exception e) {
				return e.getMessage();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	public static List<String> tailFile(final Path source, final int noOfLines) throws IOException {
		try (Stream<String> stream = Files.lines(source)) {
			FileBuffer fileBuffer = new FileBuffer(noOfLines);
			stream.forEach(fileBuffer::collect);
			return fileBuffer.getLines();
		}
	}

	private static final class FileBuffer {
		private final int noOfLines;
		private final String[] lines;
		private int offset = 0;

		public FileBuffer(int noOfLines) {
			this.noOfLines = noOfLines;
			this.lines = new String[noOfLines];
		}

		public void collect(String line) {
			lines[offset++ % noOfLines] = line;
		}

		public List<String> getLines() {
			return IntStream.range(offset < noOfLines ? 0 : offset - noOfLines, offset)
					.mapToObj(idx -> lines[idx % noOfLines]).collect(Collectors.toList());
		}
	}

}
