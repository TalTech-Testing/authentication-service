package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.*;
import ee.taltech.arete_admin_panel.repository.*;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.TokenService;
import ee.taltech.arete_admin_panel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController()
@RequestMapping("/admin")
public class BackendController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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
    public List<Student> getStudents() {

        return studentRepository.findAll();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/student/{name}")
    public StudentDto getStudent(@PathVariable("name") String name) {

        Optional<Student> student = studentRepository.findByUniid(name);
        assert student.isPresent();
        StudentDto studentDto = new StudentDto();
        for (String courseUrl : student.get().getCourses()) {
            Optional<Course> courseOptional = courseRepository.findByGitUrl(courseUrl);
            if (courseOptional.isPresent()) {
                Course course = courseOptional.get();
                CourseDto courseDto = new CourseDto();
                for (Slug slug : course.getSlugs()) {
                    Optional<StudentDataSlug> data = studentDataSlugRepository.findByStudentAndSlug(student.get(), slug);
                    if (data.isPresent()) {
                        SlugDto slugDto = new SlugDto(slug.getName(), data.get());
                        courseDto.getSlugs().add(slugDto);
                    }
                }
                studentDto.getCourses().add(courseDto);
            }

        }
        return studentDto;

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/exercises")
    public List<Course> getExercises() {

        return courseRepository.findAll();

    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/test")
    public void test() {

        areteService.testRequest();

    }

}
