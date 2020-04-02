package ee.taltech.arete_admin_panel.controller;

import arete.java.request.AreteRequest;
import arete.java.request.AreteTestUpdate;
import arete.java.response.AreteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
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
import ee.taltech.arete_admin_panel.service.UserService;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
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
@RequestMapping("services/arete/api/admin")
public class BackendController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserService userService;
    private final SubmissionRepository submissionRepository;
    private final JobRepository jobRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final SlugRepository slugRepository;
    private final SlugStudentRepository slugStudentRepository;
    private final AreteService areteService;
    private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication
    private final JwtTokenProvider jwtTokenProvider;

    public BackendController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService, SubmissionRepository submissionRepository, JobRepository jobRepository, StudentRepository studentRepository, CourseRepository courseRepository, CourseStudentRepository courseStudentRepository, SlugRepository slugRepository, SlugStudentRepository slugStudentRepository, AreteService areteService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
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
    public UserResponseIdToken getHome(@RequestBody UserPostDto userDto) throws AuthenticationException {
        try {
            LOG.info("Authenticating user {}", userDto.getUsername());
            User user = userService.getUser(userDto.getUsername());

            SHA512 sha512 = new SHA512();
            String passwordHash = sha512.get_SHA_512_SecurePassword(userDto.getPassword(), user.getSalt());

            if (!user.getPasswordHash().equals(passwordHash)) {
                throw new UserWrongCredentials("Wrong login.");
            }

            return UserResponseIdToken.builder()
                    .username(user.getUsername())
                    .color(user.getColor())
                    .id(user.getId())
                    .roles(user.getRoles())
                    .token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
                    .build();

        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }


    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/user")
    public void setUserProperties(@RequestBody UserDto userDto) throws AuthenticationException {
        try {
            User user = userService.getUser(userDto.getUsername());

            if (userDto.getColor() != null) {
                user.setColor(userDto.getColor());
            }

            userService.saveUser(user);

            LOG.info("Successfully updated {}", user.getUsername());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submissions")
    public List<Submission> getSubmissions() throws AuthenticationException {
        try {
            LOG.info("Reading all submissions");
            return submissionRepository.findTop500ByOrderByIdDesc();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submission/{hash}")
    public List<Job> getSubmission(@PathVariable("hash") String hash) throws AuthenticationException {
        try {
            LOG.info("Reading submission by hash {}", hash);
            return jobRepository.findTop10ByHashOrderByIdDesc(hash);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/students")
    public List<StudentTableDto> getStudents() throws AuthenticationException {
        try {
            LOG.info("Reading all students");
            return studentRepository
                    .findTop500ByOrderByIdDesc()
                    .stream().map(x -> objectMapper.convertValue(x, StudentTableDto.class))
                    .map(areteService::calculateFields)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/student/{id}")
    public Student getStudent(@PathVariable("id") Long id) throws AuthenticationException, NotFoundException {
        try {
            LOG.info("Reading student by id {}", id);
            Optional<Student> studentOptional = studentRepository.findById(id);
            assert studentOptional.isPresent();
            return studentOptional.get();
        } catch (AssertionError e) {
            LOG.error(e.getMessage());
            throw new NotFoundException("Selected item was not found.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/course/student/{course_student_id}")
    public CourseStudent getCourseStudent(@PathVariable("course_student_id") Long course_student_id) throws AuthenticationException, NotFoundException {
        try {
            LOG.info("Reading course student by id {}", course_student_id);
            Optional<CourseStudent> courseStudentOptional = courseStudentRepository.findById(course_student_id);
            assert courseStudentOptional.isPresent();
            return courseStudentOptional.get();
        } catch (AssertionError e) {
            LOG.error(e.getMessage());
            throw new NotFoundException("Selected item was not found.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slug/student/{slug_student_id}")
    public SlugStudent getSlugStudent(@PathVariable("slug_student_id") Long slug_student_id) throws AuthenticationException, NotFoundException {
        try {

            LOG.info("Reading slug student by id {}", slug_student_id);
            Optional<SlugStudent> slugStudentOptional = slugStudentRepository.findById(slug_student_id);
            assert slugStudentOptional.isPresent();
            return slugStudentOptional.get();

        } catch (AssertionError e) {
            LOG.error(e.getMessage());
            throw new NotFoundException("Selected item was not found.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

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
            LOG.error(e.getMessage());
            throw new NotFoundException("Selected item was not found.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

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
            LOG.error(e.getMessage());
            throw new NotFoundException("Selected item was not found.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/courses")
    public List<CourseTableDto> getCourses() throws AuthenticationException {
        try {
            LOG.info("Reading all courses");
            return courseRepository.findTop500ByOrderByIdDesc().stream().map(x -> objectMapper.convertValue(x, CourseTableDto.class)).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/course/{id}")
    public Course getCourses(@PathVariable("id") Long id) throws AuthenticationException, NotFoundException {
        try {
            LOG.info("Reading course by id {}", id);
            Optional<Course> courseOptional = courseRepository.findById(id);
            assert courseOptional.isPresent();
            return courseOptional.get();
        } catch (AssertionError e) {
            LOG.error(e.getMessage());
            throw new NotFoundException("Selected item was not found.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slugs")
    public List<SlugTableDto> getSlugs() throws AuthenticationException {
        try {
            LOG.info("Reading all slugs");
            return slugRepository.findTop500ByOrderByIdDesc().stream().map(x -> objectMapper.convertValue(x, SlugTableDto.class)).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/slug/{id}")
    public Slug getSlugs(@PathVariable("id") Long id) throws NotFoundException, AuthenticationException {
        try {
            LOG.info("Reading slug by id {}", id);
            Optional<Slug> slugOptional = slugRepository.findById(id);
            assert slugOptional.isPresent();
            return slugOptional.get();
        } catch (AssertionError e) {
            LOG.error(e.getMessage());
            throw new NotFoundException("Selected item was not found.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/job")
    public void parseJob(@RequestBody AreteResponse areteResponse) throws AuthenticationException {
        try {
            if (!areteResponse.getReturnExtra().get("shared_secret").asText().equals(System.getenv().getOrDefault("SHARED_SECRET", "Please make sure that shared_secret is set up properly"))) {
                throw new AuthenticationException("Authentication failed for submission ran for " + areteResponse.getUniid() + " with hash " + areteResponse.getHash());
            }

            LOG.info("Saving job {} into DB", areteResponse.getHash());
            areteService.enqueueAreteResponse(areteResponse);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/submissions/active")
    public AreteRequest[] getActiveSubmissions() throws AuthenticationException {
        try {
            try {
                return areteService.getActiveSubmissions();
            } catch (Exception e) {
                throw new RequestRejectedException(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

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
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

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
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/test:update")
    public void makeRequestAsync(@RequestBody AreteTestUpdate areteTestUpdate) throws AuthenticationException {
        try {
            try {
                areteService.updateTests(areteTestUpdate);
            } catch (Exception e) {
                throw new RequestRejectedException(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/image:update/{image}")
    public void makeRequestAsync(@PathVariable("image") String image) throws AuthenticationException {
        try {
            try {
                areteService.updateImage(image);
            } catch (Exception e) {
                throw new RequestRejectedException(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/debug/{boolean}")
    public String getDebug(@PathVariable("boolean") Boolean bool) throws AuthenticationException {
        try {
            try {
                return areteService.setDebug(bool) ? "Successful" : "Unsuccessful";
            } catch (Exception e) {
                return e.getMessage();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/state")
    public SystemState getState() throws AuthenticationException {
        try {
            try {
                return new SystemState();
            } catch (Exception e) {
                throw new RequestRejectedException(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }


    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/state/tester")
    public arete.java.response.SystemState getTesterState() throws AuthenticationException {
        try {
            try {
                return areteService.getTesterState();
            } catch (Exception e) {
                throw new RequestRejectedException(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

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
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

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
            LOG.error(e.getMessage());
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
