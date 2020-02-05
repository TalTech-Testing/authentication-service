package ee.taltech.arete_admin_panel.service;

import arete.java.AreteClient;
import arete.java.request.AreteRequest;
import arete.java.response.AreteResponse;
import arete.java.response.ConsoleOutput;
import arete.java.response.Error;
import arete.java.response.TestContext;
import arete.java.response.UnitTest;
import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AreteService {

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final SlugRepository slugRepository;

    private final StudentDataSlugRepository studentDataSlugRepository;

    private final JobRepository jobRepository;

    private final SubmissionRepository submissionRepository;

    private AreteClient areteClient = new AreteClient("localhost:8098");

    public AreteService(StudentRepository studentRepository, CourseRepository courseRepository, SlugRepository slugRepository, StudentDataSlugRepository studentDataSlugRepository, JobRepository jobRepository, SubmissionRepository submissionRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.slugRepository = slugRepository;
        this.studentDataSlugRepository = studentDataSlugRepository;
        this.jobRepository = jobRepository;
        this.submissionRepository = submissionRepository;
    }

    public void testRequest() {
        parseAreteResponse(areteClient.requestSync(AreteRequest.builder()
                .uniid("envomp")
                .gitStudentRepo("git@gitlab.cs.ttu.ee:envomp/iti0102-2019.git")
                .testingPlatform("python")
                .gitTestSource("https://gitlab.cs.ttu.ee/iti0102-2019/ex")
                .build()));
    }

    public void parseAreteResponse(AreteResponse response) {

        getSubmission(response);

        getJob(response);

        Student student = getStudent(response);

        Course course = getCourse(response);

        Slug slug = getSlug(response, course);

        getStudentDataSlug(response, student, slug);

    }

    private void getStudentDataSlug(AreteResponse response, Student student, Slug slug) {
        StudentDataSlug studentDataSlug;
        Optional<StudentDataSlug> optionalStudentDataSlug = studentDataSlugRepository.findByStudentAndSlug(student, slug);
        studentDataSlug = optionalStudentDataSlug.orElseGet(() -> StudentDataSlug.builder()
                .slug(slug)
                .student(student)
                .build());

        if (response.getStyle() == 100) {
            studentDataSlug.setCommitsStyleOK(studentDataSlug.getCommitsStyleOK() + 1);
        }

        int newDiagnosticErrors = response.getErrors().size();
        Map<String, Long> diagnosticErrors = response.getErrors().stream().map(Error::getKind).collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        HashSet<CodeError> diagnosticErrorSet = new HashSet<>();
        for (String key : diagnosticErrors.keySet()) {
            diagnosticErrorSet.add(new CodeError(key, Math.toIntExact(diagnosticErrors.get(key))));
        }
        studentDataSlug.getDiagnosticCodeErrors().addAll(diagnosticErrorSet);

        if (response.getFailed()) {
            studentDataSlug.setFailedCommits(studentDataSlug.getFailedCommits() + 1);
        }

        try {
            Double score = Double.parseDouble(response.getTotalGrade());
            if (score > studentDataSlug.getHighestPercentage()) {
                studentDataSlug.setHighestPercentage(score);
            }
        } catch (NumberFormatException ignored) {
        }

        int newTestErrors = 0;
        int newTestPassed = 0;
        int newTestsRan = 0;
        Map<String, Integer> testErrors = new HashMap<>();
        Set<CodeError> codeErrorSet = new HashSet<>();
        for (TestContext testContext : response.getTestSuites()) {
            for (UnitTest unitTest : testContext.getUnitTests()) {
                newTestsRan += 1;
                if (unitTest.getStatus().equals(UnitTest.TestStatus.FAILED)) {
                    newTestErrors += 1;
                    if (testErrors.containsKey(unitTest.getExceptionClass())) {
                        testErrors.put(unitTest.getExceptionClass(), testErrors.get(unitTest.getExceptionClass()) + 1);
                    } else {
                        testErrors.put(unitTest.getExceptionClass(), 1);
                    }
                }
                if (unitTest.getStatus().equals(UnitTest.TestStatus.PASSED)) {
                    newTestPassed += 1;
                }
            }
        }

        for (String key : testErrors.keySet()) {
            codeErrorSet.add(new CodeError(key, testErrors.get(key)));
        }

        studentDataSlug.getTestCodeErrors().addAll(codeErrorSet);

        studentDataSlug.setTotalCommits(studentDataSlug.getTotalCommits() + 1);
        studentDataSlug.setTotalDiagnosticErrors(studentDataSlug.getTotalDiagnosticErrors() + newDiagnosticErrors);
        studentDataSlug.setTotalTestErrors(studentDataSlug.getTotalTestErrors() + newTestErrors);
        studentDataSlug.setTotalTestsPassed(studentDataSlug.getTotalTestsPassed() + newTestPassed);
        studentDataSlug.setTotalTestsRan(studentDataSlug.getTotalTestsRan() + newTestsRan);

        studentDataSlugRepository.saveAndFlush(studentDataSlug);
    }

    private Slug getSlug(AreteResponse response, Course course) {
        Slug slug;
        Optional<Slug> optionalSlug = slugRepository.findByCourseAndName(course, response.getSlug());
        slug = optionalSlug.orElseGet(() -> Slug.builder()
                .course(course)
                .name(response.getSlug())
                .build());

        slugRepository.saveAndFlush(slug);
        return slug;
    }

    private Course getCourse(AreteResponse response) {
        Course course;
        Optional<Course> optionalCourse = courseRepository.findByGitUrl(response.getGitTestRepo());
        course = optionalCourse.orElseGet(() -> Course.builder()
                .gitUrl(response.getGitTestRepo())
                .name(response.getRoot())
                .build());

        courseRepository.saveAndFlush(course);
        return course;
    }

    private Student getStudent(AreteResponse response) {
        Student student;
        Optional<Student> optionalStudent = studentRepository.findByUniid(response.getUniid());
        student = optionalStudent.orElseGet(() -> Student.builder()
                .uniid(response.getUniid())
                .build());
        studentRepository.saveAndFlush(student);
        return student;
    }

    private void getJob(AreteResponse response) {
        Job job = Job.builder()
                .output(response.getOutput().replace("\n", "<br>"))
                .consoleOutput(response.getConsoleOutputs().stream().map(ConsoleOutput::getContent).collect(Collectors.joining()).replace("\n", "<br>"))
                .timestamp(response.getTimestamp())
                .uniid(response.getUniid())
                .slug(response.getSlug())
                .testingPlatform(response.getTestingPlatform())
                .priority(response.getPriority())
                .hash(response.getHash())
                .commitMessage(response.getCommitMessage())
                .failed(response.getFailed())
                .source(response.getFiles().stream().map(x -> x.getPath() + "<br><br>" + x.getContents().replace("\n", "<br>")).collect(Collectors.joining()))
                .gitStudentRepo(response.getGitStudentRepo())
                .gitTestRepo(response.getGitTestRepo())
                .dockerTimeout(response.getDockerTimeout())
                .dockerExtra(response.getDockerExtra())
                .systemExtra(response.getSystemExtra())
                .analyzed(0)
                .build();

        jobRepository.saveAndFlush(job);
    }

    private void getSubmission(AreteResponse response) {
        Submission submission = Submission.builder()
                .uniid(response.getUniid())
                .hash(response.getHash())
                .testingPlatform(response.getTestingPlatform())
                .root(response.getRoot())
                .timestamp(response.getTimestamp())
                .gitStudentRepo(response.getGitStudentRepo())
                .gitTestSource(response.getGitTestRepo())
                .build();

        submissionRepository.saveAndFlush(submission);
    }
}
