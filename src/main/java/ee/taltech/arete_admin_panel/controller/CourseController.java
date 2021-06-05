package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.domain.Course;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@SecurityScheme(name = "X-Docker-Token", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER,
        description = "JWT Authorization header using the Bearer scheme.\n" +
                "Enter 'Bearer'[space] and then your token in the text box below.\n" +
                "Example: Bearer eyJhbGciOiJIUzUxMiIsIn...\n" +
                "You will get the bearer from the account/login or account/register endpoint.")
@Tag(name = "course", description = "course CRUD operations")
@RestController()
@RequestMapping({"test", "services/arete/api/v2/course"})
public class CourseController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final AreteService areteService;
    private final CacheService cacheService;
    private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication

    public CourseController(AreteService areteService, CacheService cacheService, AuthenticationManager authenticationManager) {
        this.areteService = areteService;
        this.cacheService = cacheService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Returns all courses", tags = {"course"})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/all")
    public Collection<Course> getCourses() {
        return cacheService.getCourseList();
    }

    @SneakyThrows
    @Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Returns course by id", tags = {"course"})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    public Course getCoursesById(@PathVariable("id") Integer id) {
        try {
            LOG.info("Reading course by id {}", id);
            Optional<Course> courseOptional = cacheService.getCourse(id);
            assert courseOptional.isPresent();
            return courseOptional.get();
        } catch (AssertionError e) {
            throw new NotFoundException("Selected item was not found.");
        }
    }

    @Operation(
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "X-Docker-Token", description = "docker token with structure: s\"{name} {password}\"")},
            security = {@SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "X-Docker-Token")},
            summary = "Update an image on which testing takes place",
            tags = {"course"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/{image}")
    public void updateImageWithHook(@PathVariable("image") String image) {
        areteService.updateImage(image);
    }

    @Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Update an image on which testing takes place", tags = {"course"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/{image}")
    public void updateImage(@PathVariable("image") String image) {
        areteService.updateImage(image);
    }
}
