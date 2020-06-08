package ee.taltech.arete_admin_panel.controller;

import arete.java.request.AreteRequest;
import arete.java.response.AreteResponse;
import ee.taltech.arete_admin_panel.domain.Job;
import ee.taltech.arete_admin_panel.domain.Submission;
import ee.taltech.arete_admin_panel.repository.JobRepository;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.CacheService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
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
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Collection;

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "submission", description = "submission service API", externalDocs=@ExternalDocumentation(description = "More detailed explanations and examples", url = "https://github.com/envomp/arete"))
@RestController()
@RequestMapping("services/arete/api/v2/submission")
public class SubmissionController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final CacheService cacheService;
	private final AreteService areteService;
	private final JobRepository jobRepository;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication


	public SubmissionController(CacheService cacheService, AreteService areteService, JobRepository jobRepository, AuthenticationManager authenticationManager) {
		this.cacheService = cacheService;
		this.areteService = areteService;
		this.jobRepository = jobRepository;
		this.authenticationManager = authenticationManager;
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns all cached submissions", tags = {"submission"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/all")
	public Collection<Submission> getSubmissions() throws AuthenticationException {
		try {
			LOG.info("Reading all submissions");
			return cacheService.getSubmissionList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns a submission by hash", tags = {"submission"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/{hash}")
	public Collection<Job> getSubmission(@PathVariable("hash") String hash) throws AuthenticationException {
		try {
			LOG.info("Reading submission by hash {}", hash);
			return jobRepository.findTop10ByHashOrderByIdDesc(hash);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Add a new submission to database", tags = {"submission"})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(path = "/")
	public void parseJob(@RequestBody AreteResponse areteResponse) throws AuthenticationException {
		try {
			if (!areteResponse.getReturnExtra().get("shared_secret").asText().equals(System.getenv().getOrDefault("SHARED_SECRET", "Please make sure that shared_secret is set up properly"))) {
				throw new AuthenticationException("Authentication failed for submission ran for " + areteResponse.getUniid() + " with hash " + areteResponse.getHash());
			}

			LOG.info("Saving job {} into DB", areteResponse.getHash());
			areteService.enqueueAreteResponse(areteResponse);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns all currently running submissions", tags = {"submission"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/active")
	public AreteRequest[] getActiveSubmissions() throws AuthenticationException {
		try {
			try {
				return areteService.getActiveSubmissions();
			} catch (Exception e) {
				throw new RequestRejectedException(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Create a new submission which will be tested synchronously", tags = {"submission"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/:testSync")
	public AreteResponse makeRequestSync(@RequestBody AreteRequest areteRequest) throws AuthenticationException {
		try {
			try {
				return areteService.makeRequestSync(areteRequest);
			} catch (Exception e) {
				throw new RequestRejectedException(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Create a new submission which will be tested asynchronously", tags = {"submission"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/:testAsync")
	public void makeRequestAsync(@RequestBody AreteRequest areteRequest) throws AuthenticationException {
		try {
			try {
				areteService.makeRequestAsync(areteRequest);
			} catch (Exception e) {
				throw new RequestRejectedException(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

}
