package ee.taltech.arete_admin_panel.service;

import arete.java.AreteClient;
import arete.java.request.AreteRequest;
import arete.java.request.AreteTestUpdate;
import arete.java.response.Error;
import arete.java.response.SystemState;
import arete.java.response.*;
import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.repository.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional()
@EnableAsync
public class AreteService {

    private final StudentRepository studentRepository;

    private final CourseRepository courseRepository;

    private final SlugRepository slugRepository;

    private final JobRepository jobRepository;

    private final SubmissionRepository submissionRepository;

    private final SlugStudentRepository slugStudentRepository;

    private final CourseStudentRepository courseStudentRepository;

    private AreteClient areteClient = new AreteClient("http://localhost:8098");

    private Queue<AreteResponse> jobQueue = new LinkedList<>();

    private Boolean halted = false;

    public AreteService(StudentRepository studentRepository, CourseRepository courseRepository, SlugRepository slugRepository, JobRepository jobRepository, SubmissionRepository submissionRepository, SlugStudentRepository slugStudentRepository, CourseStudentRepository courseStudentRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.slugRepository = slugRepository;
        this.jobRepository = jobRepository;
        this.submissionRepository = submissionRepository;
        this.slugStudentRepository = slugStudentRepository;
        this.courseStudentRepository = courseStudentRepository;
    }

    public void enqueueAreteResponse(AreteResponse response) {
        jobQueue.add(response);
    }

    @Async
    @Scheduled(fixedRate = 100)
    public void asyncRunJob() {

        AreteResponse response = jobQueue.poll();

        if (response != null && !halted) {
            try {
                halted = true;
                parseAreteResponse(response);
            } catch (Exception e) {
            } finally {
                halted = false;
            }
        }
    }

    public void parseAreteResponse(AreteResponse response) {
        setDefaultValuesIfNull(response);

        saveSubmission(response);

        saveJob(response);

        if (!response.getFailed()) {
            Course course = getCourse(response);

            Slug slug = getSlug(response, course);

            Student student = getStudent(response, course, slug);

            SlugStudent slugStudent = getSlugStudent(slug, student);

            CourseStudent courseStudent = getCourseStudent(course, student);

            updateStudentSlugCourse(response, student, slug, course, slugStudent, courseStudent);
        }

    }

    private void setDefaultValuesIfNull(AreteResponse response) {
        if (response.getUniid() == null) {
            response.setUniid("NaN");
        }

        if (response.getErrors() == null) {
            response.setErrors(new ArrayList<>());
        }

        if (response.getFiles() == null) {
            response.setFiles(new ArrayList<>());
        }

        if (response.getTestFiles() == null) {
            response.setTestFiles(new ArrayList<>());
        }

        if (response.getTestSuites() == null) {
            response.setTestSuites(new ArrayList<>());
        }

        if (response.getConsoleOutputs() == null) {
            response.setConsoleOutputs(new ArrayList<>());
        }

        if (response.getOutput() == null) {
            response.setOutput("no output");
        }
    }

    private void updateStudentSlugCourse(AreteResponse response, Student student, Slug slug, Course course, SlugStudent slugStudent, CourseStudent courseStudent) {

        if (response.getStyle() == 100) {
            slug.setCommitsStyleOK(slug.getCommitsStyleOK() + 1);
            course.setCommitsStyleOK(course.getCommitsStyleOK() + 1);
            student.setCommitsStyleOK(student.getCommitsStyleOK() + 1);
            slugStudent.setCommitsStyleOK(slugStudent.getCommitsStyleOK() + 1);
            courseStudent.setCommitsStyleOK(courseStudent.getCommitsStyleOK() + 1);
        }

        int newDiagnosticErrors = response.getErrors().size();
        Map<String, Long> diagnosticErrors = response.getErrors().stream().map(Error::getKind).collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        for (String key : diagnosticErrors.keySet()) {

            updateDiagnosticCodeErrors(diagnosticErrors, key, slug.getDiagnosticCodeErrors());
            updateDiagnosticCodeErrors(diagnosticErrors, key, course.getDiagnosticCodeErrors());
            updateDiagnosticCodeErrors(diagnosticErrors, key, student.getDiagnosticCodeErrors());
        }

        int newTestErrors = 0;
        int newTestPassed = 0;
        int newTestsRan = 0;

        Map<String, Integer> testErrors = new HashMap<>();

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

            updateCodeErrors(testErrors, key, slug.getTestCodeErrors());
            updateCodeErrors(testErrors, key, course.getTestCodeErrors());
            updateCodeErrors(testErrors, key, student.getTestCodeErrors());
        }

        slug.setTotalCommits(slug.getTotalCommits() + 1);
        course.setTotalCommits(course.getTotalCommits() + 1);
        student.setTotalCommits(student.getTotalCommits() + 1);
        slugStudent.setTotalCommits(slugStudent.getTotalCommits() + 1);
        courseStudent.setTotalCommits(courseStudent.getTotalCommits() + 1);

        slug.setTotalDiagnosticErrors(slug.getTotalDiagnosticErrors() + newDiagnosticErrors);
        course.setTotalDiagnosticErrors(course.getTotalDiagnosticErrors() + newDiagnosticErrors);
        student.setTotalDiagnosticErrors(student.getTotalDiagnosticErrors() + newDiagnosticErrors);
        slugStudent.setTotalDiagnosticErrors(slugStudent.getTotalDiagnosticErrors() + newDiagnosticErrors);
        courseStudent.setTotalDiagnosticErrors(courseStudent.getTotalDiagnosticErrors() + newDiagnosticErrors);

        slug.setTotalTestErrors(slug.getTotalTestErrors() + newTestErrors);
        course.setTotalTestErrors(course.getTotalTestErrors() + newTestErrors);
        student.setTotalTestErrors(student.getTotalTestErrors() + newTestErrors);
        slugStudent.setTotalTestErrors(slugStudent.getTotalTestErrors() + newTestErrors);
        courseStudent.setTotalTestErrors(courseStudent.getTotalTestErrors() + newTestErrors);

        slug.setTotalTestsPassed(slug.getTotalTestsPassed() + newTestPassed);
        course.setTotalTestsPassed(course.getTotalTestsPassed() + newTestPassed);
        student.setTotalTestsPassed(student.getTotalTestsPassed() + newTestPassed);
        slugStudent.setTotalTestsPassed(slugStudent.getTotalTestsPassed() + newTestPassed);
        courseStudent.setTotalTestsPassed(courseStudent.getTotalTestsPassed() + newTestPassed);

        slug.setTotalTestsRan(slug.getTotalTestsRan() + newTestsRan);
        course.setTotalTestsRan(course.getTotalTestsRan() + newTestsRan);
        student.setTotalTestsRan(student.getTotalTestsRan() + newTestsRan);
        slugStudent.setTotalTestsRan(slugStudent.getTotalTestsRan() + newTestsRan);
        courseStudent.setTotalTestsRan(courseStudent.getTotalTestsRan() + newTestsRan);

        slugStudent.getTimestamps().add(response.getTimestamp());
        courseStudent.getTimestamps().add(response.getTimestamp());
        student.getTimestamps().add(response.getTimestamp());
        student.setLastTested(response.getTimestamp());

        try {
            Double percentage = Double.valueOf(response.getTotalGrade());
            if (percentage > slugStudent.getHighestPercent()) {
                slugStudent.setHighestPercent(percentage);
            }
        } catch (Exception ignored) {
        }

        if (response.getTimestamp() > slugStudent.getLatestSubmission()) {
            slugStudent.setLatestSubmission(response.getTimestamp());
        }
        if (response.getTimestamp() > courseStudent.getLatestSubmission()) {
            courseStudent.setLatestSubmission(response.getTimestamp());
        }

        course.getStudents().add(courseStudent);
        course.setDifferentStudents(course.getStudents().size());

        slug.getStudents().add(slugStudent);
        slug.setDifferentStudents(slug.getStudents().size());

        courseStudent.getSlugs().add(slug.getName());
        courseStudent.setDifferentSlugs(courseStudent.getSlugs().size());

        student.setDifferentCourses(student.getCourses().size());
        student.setDifferentSlugs(student.getSlugs().size());

        slugStudentRepository.saveAndFlush(slugStudent);
        courseRepository.saveAndFlush(course);
        slugRepository.saveAndFlush(slug);
        studentRepository.saveAndFlush(student);

    }

    private void updateCodeErrors(Map<String, Integer> testErrors, String key, Set<CodeError> testCodeErrors) {
        if (testCodeErrors.stream().anyMatch(x -> x.getErrorType().equals(key))) {
            testCodeErrors.stream().filter(error -> error.getErrorType().equals(key)).forEachOrdered(error -> error.setRepetitions(Math.toIntExact(error.getRepetitions() + testErrors.get(key))));
        } else {
            testCodeErrors.add(new CodeError(key, Math.toIntExact(testErrors.get(key))));
        }
    }

    private void updateDiagnosticCodeErrors(Map<String, Long> diagnosticErrors, String key, Set<CodeError> diagnosticCodeErrors) {
        if (diagnosticCodeErrors.stream().anyMatch(x -> x.getErrorType().equals(key))) {
            diagnosticCodeErrors.stream().filter(error -> error.getErrorType().equals(key)).forEachOrdered(error -> error.setRepetitions(Math.toIntExact(error.getRepetitions() + diagnosticErrors.get(key))));
        } else {
            diagnosticCodeErrors.add(new CodeError(key, Math.toIntExact(diagnosticErrors.get(key))));
        }
    }

    private Slug getSlug(AreteResponse response, Course course) {
        Slug slug;
        Optional<Slug> optionalSlug = slugRepository.findByCourseUrlAndName(course.getGitUrl(), response.getSlug());
        slug = optionalSlug.orElseGet(() -> Slug.builder()
                .courseUrl(course.getGitUrl())
                .name(response.getSlug())
                .build());

        return slug;
    }

    private Course getCourse(AreteResponse response) {
        Course course;
        Optional<Course> optionalCourse = courseRepository.findByGitUrl(response.getGitTestRepo());
        course = optionalCourse.orElseGet(() -> Course.builder()
                .gitUrl(response.getGitTestRepo())
                .name(response.getRoot())
                .build());

        return course;
    }

    private Student getStudent(AreteResponse response, Course course, Slug slug) {
        Student student;
        Optional<Student> optionalStudent = studentRepository.findByUniid(response.getUniid());
        student = optionalStudent.orElseGet(() -> Student.builder()
                .uniid(response.getUniid())
                .firstTested(response.getTimestamp())
                .lastTested(response.getTimestamp())
                .build());

        if (student.getGitRepo() == null && response.getGitStudentRepo() != null) {
            student.setGitRepo(response.getGitTestRepo());
        }

        student.getCourses().add(course.getGitUrl());
        student.getSlugs().add(slug.getName());
        return student;
    }

    private SlugStudent getSlugStudent(Slug slug, Student student) {
        slugRepository.saveAndFlush(slug);
        studentRepository.saveAndFlush(student);

        SlugStudent slugStudent;
        Optional<SlugStudent> optionalSlugStudent = slugStudentRepository.findByStudentAndSlug(student, slug);
        slugStudent = optionalSlugStudent.orElseGet(() -> SlugStudent.builder()
                .slug(slug)
                .student(student)
                .uniid(student.getUniid())
                .build());

        return slugStudent;
    }

    private CourseStudent getCourseStudent(Course course, Student student) {
        courseRepository.saveAndFlush(course);
        studentRepository.saveAndFlush(student);

        CourseStudent courseStudent;
        Optional<CourseStudent> optionalCourseStudent = courseStudentRepository.findByStudentAndCourse(student, course);
        courseStudent = optionalCourseStudent.orElseGet(() -> CourseStudent.builder()
                .course(course)
                .student(student)
                .uniid(student.getUniid())
                .build());

        return courseStudent;
    }

    private void saveJob(AreteResponse response) {
        Job job = Job.builder()
                .output(response.getOutput().replace("\n", "<br>"))
                .consoleOutput(response.getConsoleOutputs().stream().map(ConsoleOutput::getContent).collect(Collectors.joining()).replace("\n", "<br>"))
                .timestamp(response.getTimestamp())
                .uniid(response.getUniid())
                .slug(response.getSlug())
                .root(response.getRoot())
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
                .build();

        jobRepository.saveAndFlush(job);
    }

    private void saveSubmission(AreteResponse response) {
        Submission submission = Submission.builder()
                .uniid(response.getUniid())
                .slug(response.getSlug())
                .hash(response.getHash())
                .testingPlatform(response.getTestingPlatform())
                .root(response.getRoot())
                .timestamp(response.getTimestamp())
                .gitStudentRepo(response.getGitStudentRepo())
                .gitTestSource(response.getGitTestRepo())
                .build();

        submissionRepository.saveAndFlush(submission);
    }

    public AreteResponse makeRequestSync(AreteRequest areteRequest) {
        return areteClient.requestSync(areteRequest);
    }

    public void makeRequestAsync(AreteRequest areteRequest) {
        areteClient.requestAsync(areteRequest);
    }

    public void updateImage(String image) {
        areteClient.updateImage(image);
    }

    public void updateTests(AreteTestUpdate areteTestUpdate) {
        areteClient.updateTests(areteTestUpdate);
    }

    public Boolean setDebug(Boolean debug) {
        return areteClient.requestDebug(debug);
    }

    public String getTesterLogs() {
        return areteClient.requestLogs();
    }

    public SystemState getTesterState() {
        return areteClient.requestState();
    }

    public AreteRequest[] getActiveSubmissions() {
        return areteClient.requestActiveSubmissions();
    }
}
