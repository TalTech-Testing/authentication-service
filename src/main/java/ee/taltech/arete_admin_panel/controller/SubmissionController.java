package ee.taltech.arete_admin_panel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ee.taltech.arete.java.request.AreteRequestDTO;
import ee.taltech.arete.java.request.hook.AreteTestUpdateDTO;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete_admin_panel.domain.Job;
import ee.taltech.arete_admin_panel.domain.Submission;
import ee.taltech.arete_admin_panel.repository.JobRepository;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.CacheService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Collection;

@SecurityScheme(name = "X-Testing-Token", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "submission", description = "submission service API", externalDocs = @ExternalDocumentation(description = "More detailed explanations and examples", url = "https://github.com/envomp/arete"))
@RestController()
@RequestMapping("services/arete/api/v2/submission")
public class SubmissionController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final ObjectMapper objectMapper;
	private final CacheService cacheService;
	private final AreteService areteService;
	private final JobRepository jobRepository;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication


	public SubmissionController(ObjectMapper objectMapper,
								CacheService cacheService,
								AreteService areteService,
								JobRepository jobRepository,
								AuthenticationManager authenticationManager) {
		this.objectMapper = objectMapper;
		this.cacheService = cacheService;
		this.areteService = areteService;
		this.jobRepository = jobRepository;
		this.authenticationManager = authenticationManager;
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Returns all cached submissions", tags = {"submission"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/all")
	public Collection<Submission> getSubmissions() {
		return cacheService.getSubmissionList();
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Returns a submission by hash", tags = {"submission"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/{hash}")
	public Collection<Job> getSubmission(@PathVariable("hash") String hash) {
		LOG.info("Reading submission by hash {}", hash);
		return jobRepository.findTop10ByHashOrderByIdDesc(hash);
	}

	@SneakyThrows
	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Add a new submission to database", tags = {"submission"})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(path = "")
	public void parseJob(@RequestBody AreteResponseDTO areteResponse) {
		if (!areteResponse.getReturnExtra().get("shared_secret").asText().equals(System.getenv().getOrDefault("SHARED_SECRET", "Please make sure that shared_secret is set up properly"))) {
			throw new AuthenticationException("Authentication failed for submission ran for " + areteResponse.getUniid() + " with hash " + areteResponse.getHash());
		}
		areteService.enqueueAreteResponse(areteResponse);
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Returns all currently running submissions", tags = {"submission"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/active")
	public AreteRequestDTO[] getActiveSubmissions() {
		return areteService.getActiveSubmissions();
	}

	@Operation(
			parameters = {
					@Parameter(in = ParameterIn.HEADER, name = "X-Gitlab-Token",
							description = "gitlab token with structure: s\"{name} {password}\""),
					@Parameter(in = ParameterIn.HEADER, name = "X-Testing-Token",
							description = "testing token with structure: s\"{name} {password}\"")
			},
			security = {
					@SecurityRequirement(name = "Authorization"),
					@SecurityRequirement(name = "X-Gitlab-Token"),
					@SecurityRequirement(name = "X-Testing-Token")
			}, summary = "Create a new submission which will be tested synchronously", tags = {"submission"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/:testSync")
	@SneakyThrows
	public AreteResponseDTO makeRequestSync(HttpEntity<String> httpEntity) {
		AreteRequestDTO requestDTO = objectMapper.readValue(httpEntity.getBody(), AreteRequestDTO.class);
		return areteService.makeRequestSync(requestDTO);
	}

	@Operation(
			parameters = {
					@Parameter(in = ParameterIn.HEADER, name = "X-Gitlab-Token",
							description = "gitlab token with structure: s\"{name} {password}\""),
					@Parameter(in = ParameterIn.HEADER, name = "X-Testing-Token",
							description = "testing token with structure: s\"{name} {password}\"")
			},
			security = {
					@SecurityRequirement(name = "Authorization"),
					@SecurityRequirement(name = "X-Gitlab-Token"),
					@SecurityRequirement(name = "X-Testing-Token")
			}, summary = "Create a new submission which will be tested asynchronously", tags = {"submission"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/:testAsync")
	@SneakyThrows
	public void makeRequestAsync(HttpEntity<String> httpEntity) {
		AreteRequestDTO requestDTO = objectMapper.readValue(httpEntity.getBody(), AreteRequestDTO.class);
		areteService.makeRequestAsync(requestDTO);
	}

	@Operation(
			parameters = {
					@Parameter(in = ParameterIn.HEADER, name = "X-Gitlab-Token",
							description = "gitlab token with structure: s\"{name} {password}\""),
					@Parameter(in = ParameterIn.HEADER, name = "X-Testing-Token",
							description = "testing token with structure: s\"{name} {password}\"")
			},
			security = {
					@SecurityRequirement(name = "Authorization"),
					@SecurityRequirement(name = "X-Gitlab-Token"),
					@SecurityRequirement(name = "X-Testing-Token")
			},
			summary = "Run tests from webhook",
			tags = {"submission"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PostMapping("/:webhook/withTests")
	public void makeRequestAsyncWebHook(@RequestBody AreteTestUpdateDTO areteTestUpdate, @RequestParam(name = "testRepository") String testRepository) {
		areteService.makeRequestWebhook(areteTestUpdate, testRepository);
	}
}
