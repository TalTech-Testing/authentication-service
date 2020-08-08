package ee.taltech.arete_admin_panel.controller;

import arete.java.request.AreteTestUpdate;
import ee.taltech.arete_admin_panel.domain.Slug;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugTableDto;
import ee.taltech.arete_admin_panel.repository.SlugRepository;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Collection;
import java.util.Optional;

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "exercise", description = "exercise CRUD operations")
@RestController()
@RequestMapping("services/arete/api/v2/exercise")
public class ExerciseController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final SlugRepository slugRepository;
	private final CacheService cacheService;
	private final AreteService areteService;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication

	public ExerciseController(SlugRepository slugRepository, CacheService cacheService, AreteService areteService, AuthenticationManager authenticationManager) {
		this.slugRepository = slugRepository;
		this.cacheService = cacheService;
		this.areteService = areteService;
		this.authenticationManager = authenticationManager;
	}


	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns all exercises", tags = {"exercise"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/all")
	public Collection<SlugTableDto> getSlugs() throws AuthenticationException {
		try {
			LOG.info("Reading all slugs");
			return cacheService.getSlugList();
		} catch (Exception e) {
			
			throw new AuthenticationException(e.getMessage());
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns exercise by id", tags = {"exercise"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/{id}")
	public Slug getSlugsById(@PathVariable("id") Long id) throws NotFoundException, AuthenticationException {
		try {
			LOG.info("Reading slug by id {}", id);
			Optional<Slug> slugOptional = slugRepository.findById(id);
			assert slugOptional.isPresent();
			return slugOptional.get();
		} catch (AssertionError e) {
			
			throw new NotFoundException("Selected item was not found.");
		} catch (Exception e) {
			
			throw new AuthenticationException(e.getMessage());
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Update an exercise", tags = {"exercise"})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@PutMapping("")
	@PostMapping("")
	public void makeRequestAsync(@RequestBody AreteTestUpdate areteTestUpdate) throws AuthenticationException {
		try {
			try {
				areteService.updateTests(areteTestUpdate);
			} catch (Exception e) {
				throw new RequestRejectedException(e.getMessage());
			}
		} catch (Exception e) {
			throw new AuthenticationException(e.getMessage());
		}
	}

}
