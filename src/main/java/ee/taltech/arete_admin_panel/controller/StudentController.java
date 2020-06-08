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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Collection;
import java.util.Optional;

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "student", description = "student CRUD operations")
@RestController()
@RequestMapping("services/arete/api/admin")
public class StudentController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final CacheService cacheService;
	private final StudentRepository studentRepository;
	private final CourseStudentRepository courseStudentRepository;
	private final SlugStudentRepository slugStudentRepository;
	private final CourseRepository courseRepository;
	private final SlugRepository slugRepository;
	private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication

	public StudentController(AuthenticationManager authenticationManager, CacheService cacheService, StudentRepository studentRepository, CourseStudentRepository courseStudentRepository, SlugStudentRepository slugStudentRepository, CourseRepository courseRepository, SlugRepository slugRepository) {
		this.authenticationManager = authenticationManager;
		this.cacheService = cacheService;
		this.studentRepository = studentRepository;
		this.courseStudentRepository = courseStudentRepository;
		this.slugStudentRepository = slugStudentRepository;
		this.courseRepository = courseRepository;
		this.slugRepository = slugRepository;
	}


	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns all cached students", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/students")
	public Collection<StudentTableDto> getStudents() throws AuthenticationException {
		try {
			LOG.info("Reading all students");
			return cacheService.getStudentList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns student with id", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/student/{id}")
	public Student getStudent(@PathVariable("id") Long id) throws AuthenticationException, NotFoundException {
		try {
			LOG.info("Reading student by id {}", id);
			Optional<Student> studentOptional = studentRepository.findById(id);
			assert studentOptional.isPresent();
			return studentOptional.get();
		} catch (AssertionError e) {
			e.printStackTrace();
			throw new NotFoundException("Selected item was not found.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns students' activity regarding a course by its id", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/course/student/{course_student_id}")
	public CourseStudent getCourseStudentById(@PathVariable("course_student_id") Long course_student_id) throws AuthenticationException, NotFoundException {
		try {
			LOG.info("Reading course student by id {}", course_student_id);
			Optional<CourseStudent> courseStudentOptional = courseStudentRepository.findById(course_student_id);
			assert courseStudentOptional.isPresent();
			return courseStudentOptional.get();
		} catch (AssertionError e) {
			e.printStackTrace();
			throw new NotFoundException("Selected item was not found.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns students' activity regarding an exercise by its id", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/slug/student/{slug_student_id}")
	public SlugStudent getSlugStudentById(@PathVariable("slug_student_id") Long slug_student_id) throws AuthenticationException, NotFoundException {
		try {

			LOG.info("Reading slug student by id {}", slug_student_id);
			Optional<SlugStudent> slugStudentOptional = slugStudentRepository.findById(slug_student_id);
			assert slugStudentOptional.isPresent();
			return slugStudentOptional.get();

		} catch (AssertionError e) {
			e.printStackTrace();
			throw new NotFoundException("Selected item was not found.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns students' activity regarding a course by course id and student id", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/course/{course_id}/student/{student_id}")
	public CourseStudent getCourseStudent(@PathVariable("student_id") Long student_id, @PathVariable("course_id") Long course_id) throws AuthenticationException, NotFoundException {
		try {

			LOG.info("Reading course {} student by id {}", course_id, student_id);
			Optional<Student> studentOptional = studentRepository.findById(student_id);
			assert studentOptional.isPresent();
			Optional<Course> courseOptional = courseRepository.findById(course_id);
			assert courseOptional.isPresent();
			Optional<CourseStudent> courseStudentOptional = courseStudentRepository.findByStudentAndCourse(studentOptional.get(), courseOptional.get());
			assert courseStudentOptional.isPresent();
			return courseStudentOptional.get();

		} catch (AssertionError e) {
			e.printStackTrace();
			throw new NotFoundException("Selected item was not found.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

	@Operation(security = {@SecurityRequirement(name = "Authorization")},summary = "Returns students' activity regarding an exercise by exercise id and student id", tags = {"student"})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(path = "/slug/{slug_id}/student/{student_id}")
	public SlugStudent getSlugStudent(@PathVariable("student_id") Long student_id, @PathVariable("slug_id") Long slug_id) throws NotFoundException, AuthenticationException {
		try {
			LOG.info("Reading slug {} student by id {}", slug_id, student_id);
			Optional<Student> studentOptional = studentRepository.findById(student_id);
			assert studentOptional.isPresent();
			Optional<Slug> slugOptional = slugRepository.findById(slug_id);
			assert slugOptional.isPresent();
			Optional<SlugStudent> slugStudentOptional = slugStudentRepository.findByStudentAndSlug(studentOptional.get(), slugOptional.get());
			assert slugStudentOptional.isPresent();
			return slugStudentOptional.get();
		} catch (AssertionError e) {
			e.printStackTrace();
			throw new NotFoundException("Selected item was not found.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Not authorized.");
		}
	}

}
