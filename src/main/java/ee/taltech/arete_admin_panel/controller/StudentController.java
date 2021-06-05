package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentTableDto;
import ee.taltech.arete_admin_panel.repository.*;
import ee.taltech.arete_admin_panel.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
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

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "student", description = "student CRUD operations")
@RestController()
@RequestMapping("services/arete/api/v2/student")
public class StudentController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final CacheService cacheService;
	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;
	private final SlugRepository slugRepository;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication

	public StudentController(AuthenticationManager authenticationManager, CacheService cacheService, StudentRepository studentRepository, CourseRepository courseRepository, SlugRepository slugRepository) {
		this.authenticationManager = authenticationManager;
		this.cacheService = cacheService;
		this.studentRepository = studentRepository;
		this.courseRepository = courseRepository;
		this.slugRepository = slugRepository;
	}


	@SneakyThrows
	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Returns all cached students", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/all")
	public Collection<StudentTableDto> getStudents() {
		return cacheService.getStudentList();
	}

	@SneakyThrows
	@Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Returns student with id", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/{id}")
	public Student getStudent(@PathVariable("id") Long id) {
		try {
			LOG.info("Reading student by id {}", id);
			Optional<Student> studentOptional = studentRepository.findById(id);
			assert studentOptional.isPresent();
			return studentOptional.get();
		} catch (AssertionError e) {
			throw new NotFoundException("Selected item was not found.");
		}
	}
}
