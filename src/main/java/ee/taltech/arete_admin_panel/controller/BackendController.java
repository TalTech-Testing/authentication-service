package ee.taltech.arete_admin_panel.controller;

import arete.java.request.AreteRequest;
import arete.java.request.AreteTestUpdate;
import arete.java.response.AreteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugTableDto;
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
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    private final CourseStudentRepository courseStudentRepository;
    private final SlugRepository slugRepository;
    private final SlugStudentRepository slugStudentRepository;
    private final AreteService areteService;

    public BackendController(UserService userService, TokenService tokenService, SubmissionRepository submissionRepository, JobRepository jobRepository, StudentRepository studentRepository, CourseRepository courseRepository, CourseStudentRepository courseStudentRepository, SlugRepository slugRepository, SlugStudentRepository slugStudentRepository, AreteService areteService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.submissionRepository = submissionRepository;
        this.jobRepository = jobRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.slugRepository = slugRepository;
        this.slugStudentRepository = slugStudentRepository;
        this.areteService = areteService;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/auth")
    public UserResponseIdToken getHome(@RequestBody UserPostDto userDto) {
        LOG.info("Authenticating user {}", userDto.getUsername());
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

        LOG.info("Successfully updated {}", user.getUsername());

    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submissions")
    public List<Submission> getSubmissions() {

        LOG.info("Reading all submissions");
        return submissionRepository.findAll();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submission/{hash}")
    public List<Job> getSubmission(@PathVariable("hash") String hash) {

        LOG.info("Reading submission by hash {}", hash);
        return jobRepository.findByHash(hash);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/students")
    public List<StudentTableDto> getStudents() {

        LOG.info("Reading all students");
        return studentRepository.findAll().stream().map(x -> objectMapper.convertValue(x, StudentTableDto.class)).collect(Collectors.toList());

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/student/{id}")
    public Student getStudent(@PathVariable("id") Long id) {
        LOG.info("Reading student by id {}", id);
        Optional<Student> studentOptional = studentRepository.findById(id);
        assert studentOptional.isPresent();
        return studentOptional.get();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/course/student/{id}")
    public Student getCourseStudent(@PathVariable("id") Long id) {
        LOG.info("Reading student by id {}", id);
        Optional<CourseStudent> studentOptional = courseStudentRepository.findById(id);
        assert studentOptional.isPresent();
        return studentOptional.get().getStudent();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slug/student/{id}")
    public Student getSlugStudent(@PathVariable("id") Long id) {
        LOG.info("Reading student by id {}", id);
        Optional<SlugStudent> studentOptional = slugStudentRepository.findById(id);
        assert studentOptional.isPresent();
        return studentOptional.get().getStudent();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/courses")
    public List<CourseTableDto> getCourses() {

        LOG.info("Reading all courses");
        return courseRepository.findAll().stream().map(x -> objectMapper.convertValue(x, CourseTableDto.class)).collect(Collectors.toList());

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/course/{id}")
    public Course getCourses(@PathVariable("id") Long id) {

        LOG.info("Reading course by id {}", id);
        Optional<Course> courseOptional = courseRepository.findById(id);
        assert courseOptional.isPresent();
        return courseOptional.get();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slugs")
    public List<SlugTableDto> getSlugs() {

        LOG.info("Reading all slugs");
        return slugRepository.findAll().stream().map(x -> objectMapper.convertValue(x, SlugTableDto.class)).collect(Collectors.toList());

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slug/{id}")
    public Slug getSlugs(@PathVariable("id") Long id) {

        LOG.info("Reading slug by id {}", id);
        Optional<Slug> slugOptional = slugRepository.findById(id);
        assert slugOptional.isPresent();
        return slugOptional.get();

    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/job")
    public void parseJob(@RequestBody AreteResponse areteResponse) {

        LOG.info("Saving job {} into DB", areteResponse.getHash());
        areteService.enqueueAreteResponse(areteResponse);

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/submissions/active")
    public AreteRequest[] getActiveSubmissions() {

        try {
            return areteService.getActiveSubmissions();
        } catch (Exception e) {
            throw new RequestRejectedException(e.getMessage());
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/:testSync")
    public AreteResponse makeRequestSync(@RequestBody AreteRequest areteRequest) {

        try {
            return areteService.makeRequestSync(areteRequest);
        } catch (Exception e) {
            throw new RequestRejectedException(e.getMessage());
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/:testAsync")
    public void makeRequestAsync(@RequestBody AreteRequest areteRequest) {

        try {
            areteService.makeRequestAsync(areteRequest);
        } catch (Exception e) {
            throw new RequestRejectedException(e.getMessage());
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/test:update")
    public void makeRequestAsync(@RequestBody AreteTestUpdate areteTestUpdate) {

        try {
            areteService.updateTests(areteTestUpdate);
        } catch (Exception e) {
            throw new RequestRejectedException(e.getMessage());
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/image:update/{image}")
    public void makeRequestAsync(@PathVariable("image") String image) {

        try {
            areteService.updateImage(image);
        } catch (Exception e) {
            throw new RequestRejectedException(e.getMessage());
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/debug/{boolean}")
    public String getDebug(@PathVariable("boolean") Boolean bool) {

        try {
            return areteService.setDebug(bool) ? "Successful" : "Unsuccessful";
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/logs")
    public String getLogs() {

        try {
            return String.join("", tailFile(Paths.get("logs/spring.log"), 2000));
        } catch (IOException e) {
            return e.getMessage();
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/logs/tester")
    public String getTesterLogs() {

        try {
            return areteService.getTesterLogs();
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/state")
    public SystemState getState() {

        try {
            return new SystemState();
        } catch (Exception e) {
            throw new RequestRejectedException(e.getMessage());
        }

    }


    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/state/tester")
    public arete.java.response.SystemState getTesterState() {

        try {
            return areteService.getTesterState();
        } catch (Exception e) {
            throw new RequestRejectedException(e.getMessage());
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
