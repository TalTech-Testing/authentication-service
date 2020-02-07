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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
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

        saveSubmission(response);

        saveJob(response);

        Course course = getCourse(response);

        Slug slug = getSlug(response, course);

        Student student = getStudent(response, course, slug);

        saveStudentDataSlug(response, student, slug, course);

    }

    private void saveStudentDataSlug(AreteResponse response, Student student, Slug slug, Course course) {
        StudentDataSlug studentDataSlug;
        Optional<StudentDataSlug> optionalStudentDataSlug = studentDataSlugRepository.findByStudentAndSlug(student, slug);
        studentDataSlug = optionalStudentDataSlug.orElseGet(() -> StudentDataSlug.builder()
                .slug(slug)
                .course(course)
                .student(student)
                .build());

        if (response.getStyle() == 100) {
            studentDataSlug.setCommitsStyleOK(studentDataSlug.getCommitsStyleOK() + 1);
            slug.setCommitsStyleOK(slug.getCommitsStyleOK() + 1);
            course.setCommitsStyleOK(course.getCommitsStyleOK() + 1);
            student.setCommitsStyleOK(student.getCommitsStyleOK() + 1);
        }

        int newDiagnosticErrors = response.getErrors().size();
        Map<String, Long> diagnosticErrors = response.getErrors().stream().map(Error::getKind).collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        for (String key : diagnosticErrors.keySet()) {

            updateDiagnosticCodeErrors(diagnosticErrors, key, studentDataSlug.getDiagnosticCodeErrors());

            updateDiagnosticCodeErrors(diagnosticErrors, key, slug.getDiagnosticCodeErrors());

            updateDiagnosticCodeErrors(diagnosticErrors, key, course.getDiagnosticCodeErrors());

            updateDiagnosticCodeErrors(diagnosticErrors, key, student.getDiagnosticCodeErrors());

        }

        if (response.getFailed()) {
            studentDataSlug.setFailedCommits(studentDataSlug.getFailedCommits() + 1);
            slug.setFailedCommits(slug.getFailedCommits() + 1);
            course.setFailedCommits(course.getFailedCommits() + 1);
            student.setFailedCommits(student.getFailedCommits() + 1);
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

            updateCodeErrors(testErrors, key, studentDataSlug.getTestCodeErrors());

            updateCodeErrors(testErrors, key, slug.getTestCodeErrors());

            updateCodeErrors(testErrors, key, course.getTestCodeErrors());

            updateCodeErrors(testErrors, key, student.getTestCodeErrors());

        }

        studentDataSlug.setTotalCommits(studentDataSlug.getTotalCommits() + 1);
        slug.setTotalCommits(slug.getTotalCommits() + 1);
        course.setTotalCommits(course.getTotalCommits() + 1);
        student.setTotalCommits(student.getTotalCommits() + 1);

        studentDataSlug.setTotalDiagnosticErrors(studentDataSlug.getTotalDiagnosticErrors() + newDiagnosticErrors);
        slug.setTotalDiagnosticErrors(slug.getTotalDiagnosticErrors() + newDiagnosticErrors);
        course.setTotalDiagnosticErrors(course.getTotalDiagnosticErrors() + newDiagnosticErrors);
        student.setTotalDiagnosticErrors(student.getTotalDiagnosticErrors() + newDiagnosticErrors);

        studentDataSlug.setTotalTestErrors(studentDataSlug.getTotalTestErrors() + newTestErrors);
        slug.setTotalTestErrors(slug.getTotalTestErrors() + newTestErrors);
        course.setTotalTestErrors(course.getTotalTestErrors() + newTestErrors);
        student.setTotalTestErrors(student.getTotalTestErrors() + newTestErrors);

        studentDataSlug.setTotalTestsPassed(studentDataSlug.getTotalTestsPassed() + newTestPassed);
        slug.setTotalTestsPassed(slug.getTotalTestsPassed() + newTestPassed);
        course.setTotalTestsPassed(course.getTotalTestsPassed() + newTestPassed);
        student.setTotalTestsPassed(student.getTotalTestsPassed() + newTestPassed);

        studentDataSlug.setTotalTestsRan(studentDataSlug.getTotalTestsRan() + newTestsRan);
        slug.setTotalTestsRan(slug.getTotalTestsRan() + newTestsRan);
        course.setTotalTestsRan(course.getTotalTestsRan() + newTestsRan);
        student.setTotalTestsRan(student.getTotalTestsRan() + newTestsRan);

        studentDataSlugRepository.saveAndFlush(studentDataSlug);
        studentRepository.saveAndFlush(student);
        slugRepository.saveAndFlush(slug);
        courseRepository.saveAndFlush(course);

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

        slugRepository.saveAndFlush(slug);

        course.getSlugs().add(slug);
        courseRepository.saveAndFlush(course);

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

    private Student getStudent(AreteResponse response, Course course, Slug slug) {
        Student student;
        Optional<Student> optionalStudent = studentRepository.findByUniid(response.getUniid());
        student = optionalStudent.orElseGet(() -> Student.builder()
                .uniid(response.getUniid())
                .firstTested(response.getTimestamp())
                .build());

        if (student.getGitRepo() == null && response.getGitStudentRepo() != null) {
            student.setGitRepo(response.getGitTestRepo());
        }

        student.getCourses().add(course.getGitUrl());
        studentRepository.saveAndFlush(student);

        slug.getStudents().add(student);
        slugRepository.saveAndFlush(slug);

        return student;
    }

    private void saveJob(AreteResponse response) {
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

    private void saveSubmission(AreteResponse response) {
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
