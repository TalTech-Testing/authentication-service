package ee.taltech.arete_admin_panel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.EmptyCourseDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.FullCourseDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.EmptySlugDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.FullSlugDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.EmptyStudentDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentDataSlugTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserPostDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseIdToken;
import ee.taltech.arete_admin_panel.repository.*;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.TokenService;
import ee.taltech.arete_admin_panel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/admin")
public class BackendController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserService userService;
    private final TokenService tokenService;
    private final SubmissionRepository submissionRepository;
    private final JobRepository jobRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SlugRepository slugRepository;
    private final StudentDataSlugRepository studentDataSlugRepository;
    private final AreteService areteService;

    public BackendController(UserService userService, TokenService tokenService, SubmissionRepository submissionRepository, JobRepository jobRepository, StudentRepository studentRepository, CourseRepository courseRepository, SlugRepository slugRepository, StudentDataSlugRepository studentDataSlugRepository, AreteService areteService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.submissionRepository = submissionRepository;
        this.jobRepository = jobRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.slugRepository = slugRepository;
        this.studentDataSlugRepository = studentDataSlugRepository;
        this.areteService = areteService;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/auth")
    public UserResponseIdToken getHome(@RequestBody UserPostDto userDto) {
        User user = userService.getUser(userDto.getUsername());

        SHA512 sha512 = new SHA512();
        String passwordHash = sha512.get_SHA_512_SecurePassword(userDto.getPassword(), user.getSalt());

        if (!user.getPasswordHash().equals(passwordHash)) {
            throw new UserWrongCredentials("Wrong login.");
        }

        return tokenService.createResponse(userService.getHome(user.getUsername()));
    }


    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/user")
    public void setUserProperties(@RequestBody UserDto userDto) {

        User user = userService.getUser(userDto.getUsername());

        if (!tokenService.verifyTokenIsCertainId(userDto.getToken(), user.getId())) {
            throw new UserWrongCredentials("Bad token.");
        }

        if (userDto.getColor() != null) {
            user.setColor(userDto.getColor());
        }

        userService.saveUser(user);

    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submissions")
    public List<Submission> getSubmissions() {

        return submissionRepository.findAll();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submission/{hash}")
    public List<Job> getSubmission(@PathVariable("hash") String hash) {

        return jobRepository.findByHash(hash);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/students")
    public List<StudentTableDto> getStudents() {

        return studentRepository.findAll().stream().map(x -> objectMapper.convertValue(x, StudentTableDto.class)).collect(Collectors.toList());

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/student/{id}")
    public EmptyStudentDto getStudent(@PathVariable("id") Long id) {
        Optional<Student> student = studentRepository.findById(id);
        assert student.isPresent();
        EmptyStudentDto emptyStudentDto = new EmptyStudentDto();
        for (String courseUrl : student.get().getCourses()) {
            Optional<Course> courseOptional = courseRepository.findByGitUrl(courseUrl);
            if (courseOptional.isPresent()) {
                Course course = courseOptional.get();
                EmptyCourseDto emptyCourseDto = new EmptyCourseDto();
                emptyCourseDto.setName(course.getName());
                for (Slug slug : course.getSlugs()) {
                    Optional<StudentDataSlug> data = studentDataSlugRepository.findByStudentAndSlug(student.get(), slug);
                    if (data.isPresent()) {
                        EmptySlugDto emptySlugDto = new EmptySlugDto(slug.getName(), objectMapper.convertValue(data.get(), StudentDataSlugTableDto.class));
                        emptyCourseDto.getSlugs().add(emptySlugDto);
                    }
                }
                emptyStudentDto.getCourses().add(emptyCourseDto);
            }

        }
        return emptyStudentDto;

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/courses")
    public List<CourseTableDto> getCourses() {

        return courseRepository.findAll().stream().map(x -> objectMapper.convertValue(x, CourseTableDto.class)).collect(Collectors.toList());

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/course/{id}")
    public FullCourseDto getCourses(@PathVariable("id") Long id) {

        Optional<Course> courseOptional = courseRepository.findById(id);
        assert courseOptional.isPresent();
        return objectMapper.convertValue(courseOptional.get(), FullCourseDto.class);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slugs")
    public List<SlugTableDto> getSlugs() {

        return slugRepository.findAll().stream().map(x -> objectMapper.convertValue(x, SlugTableDto.class)).collect(Collectors.toList());

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slug/{id}")
    public FullSlugDto getSlugs(@PathVariable("id") Long id) {

        Optional<Slug> slugOptional = slugRepository.findById(id);
        assert slugOptional.isPresent();
        return objectMapper.convertValue(slugOptional.get(), FullSlugDto.class);

    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/test")
    public void test() {

        areteService.testRequest();

    }

}
