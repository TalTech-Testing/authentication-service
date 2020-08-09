package ee.taltech.arete_admin_panel.service;

import arete.java.AreteClient;
import arete.java.request.AreteRequest;
import arete.java.request.AreteTestUpdate;
import arete.java.response.ConsoleOutput;
import arete.java.response.Error;
import arete.java.response.SystemState;
import arete.java.response.TestContext;
import arete.java.response.UnitTest;
import arete.java.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private final CacheService cacheService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;
	private final SlugRepository slugRepository;
	private final SubmissionRepository submissionRepository;
	private final JobRepository jobRepository;
	private final SlugStudentRepository slugStudentRepository;
	private final CourseStudentRepository courseStudentRepository;

	private AreteClient areteClient = new AreteClient(System.getProperty("TESTER_URL", "http://localhost:8098"));
	private Queue<AreteResponse> jobQueue = new LinkedList<>();
	private Boolean halted = false;

	public AreteService(CacheService cacheService, StudentRepository studentRepository, CourseRepository courseRepository, SlugRepository slugRepository, JobRepository jobRepository, SubmissionRepository submissionRepository, SlugStudentRepository slugStudentRepository, CourseStudentRepository courseStudentRepository) {
		this.cacheService = cacheService;
		this.studentRepository = studentRepository;
		this.courseRepository = courseRepository;
		this.slugRepository = slugRepository;
		this.jobRepository = jobRepository;
		this.submissionRepository = submissionRepository;
		this.slugStudentRepository = slugStudentRepository;
		this.courseStudentRepository = courseStudentRepository;
	}

	public void enqueueAreteResponse(AreteResponse response) {
		LOG.info("Saving job into DB: {}", response);
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
			} catch (Exception ignored) {
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
			updateDiagnosticCodeErrors(diagnosticErrors, key, courseStudent.getDiagnosticCodeErrors());
			updateDiagnosticCodeErrors(diagnosticErrors, key, slugStudent.getDiagnosticCodeErrors());
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
			updateCodeErrors(testErrors, key, courseStudent.getTestCodeErrors());
			updateCodeErrors(testErrors, key, slugStudent.getTestCodeErrors());
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

		updateCourseStudent(courseStudent, courseStudent.getId());
		updateSlugStudent(slugStudent, slugStudent.getId());
		updateCourse(course, course.getId());
		updateSlug(slug, slug.getId());
		updateStudent(student, student.getId());

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
				.testSuites(response.getTestSuites().stream()
						.map(x -> ee.taltech.arete_admin_panel.domain.TestContext.builder()
								.endDate(x.getEndDate())
								.file(x.getFile())
								.grade(x.getGrade())
								.name(x.getName())
								.passedCount(x.getPassedCount())
								.startDate(x.getStartDate())
								.weight(x.getWeight())
								.unitTests(
										x.getUnitTests().stream()
												.map(y -> ee.taltech.arete_admin_panel.domain.UnitTest.builder()
														.exceptionClass(y.getExceptionClass())
														.exceptionMessage(y.getExceptionMessage())
														.groupsDependedUpon(y.getGroupsDependedUpon())
														.methodsDependedUpon(y.getMethodsDependedUpon())
														.printExceptionMessage(y.getPrintExceptionMessage())
														.printStackTrace(y.getPrintStackTrace())
														.stackTrace(y.getStackTrace())
														.name(y.getName())
														.status(y.getStatus().toString())
														.timeElapsed(y.getTimeElapsed())
														.weight(y.getWeight())
														.build())
												.collect(Collectors.toList()))
								.build())
						.collect(Collectors.toList()))
				.timestamp(response.getTimestamp())
				.uniid(response.getUniid())
				.slug(response.getSlug())
				.root(response.getRoot())
				.testingPlatform(response.getTestingPlatform())
				.priority(response.getPriority())
				.hash(response.getHash())
				.commitMessage(response.getCommitMessage())
				.failed(response.getFailed())
				.gitStudentRepo(response.getGitStudentRepo())
				.gitTestRepo(response.getGitTestRepo())
				.dockerTimeout(response.getDockerTimeout())
				.dockerExtra(response.getDockerExtra())
				.systemExtra(response.getSystemExtra())
				.build();

		jobRepository.saveAndFlush(job);
	}

	public Submission saveSubmission(AreteResponse response) {
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

		updateSubmissions(submission, submission.getHash());
		return submission;
	}

	public AreteResponse makeRequestSync(AreteRequest areteRequest) {
		LOG.info("Forwarding a sync submission: {}", areteRequest);
		return areteClient.requestSync(areteRequest);
	}

	public void makeRequestAsync(AreteRequest areteRequest) {
		LOG.info("Forwarding a async submission: {}", areteRequest);
		areteClient.requestAsync(areteRequest);
	}

	public void makeRequestWebhook(AreteTestUpdate update, String testRepository) {
		AreteTestUpdate.Commit latest = update.getCommits().get(0);

		Set<String> slugs = new HashSet<>();
		slugs.addAll(latest.getAdded());
		slugs.addAll(latest.getModified());

		AreteRequest request = AreteRequest.builder()
				.eMail(latest.getAuthor().getEmail())
				.uniid(latest.getAuthor().getName())
				.gitStudentRepo(update.getProject().getUrl())
				.gitTestSource(testRepository)
				.slugs(slugs)
				.build();

		makeRequestAsync(request);
	}

	public void updateImage(String image) {
		LOG.info("Updating image: {}", image);
		areteClient.updateImage(image);
	}

	public void updateTests(AreteTestUpdate areteTestUpdate) {
		LOG.info("Updating tests: {}", areteTestUpdate);
		areteClient.updateTests(areteTestUpdate);
	}

	public String getTesterLogs() {
		LOG.info("Reading tester logs");
		return areteClient.requestLogs();
	}

	public SystemState getTesterState() {
		LOG.info("Reading tester state");
		return areteClient.requestState();
	}

	public AreteRequest[] getActiveSubmissions() {
		LOG.info("Reading all active submissions");
		return areteClient.requestActiveSubmissions();
	}

	/////// CACHING

	public CourseStudent updateCourseStudent(CourseStudent courseStudent, Long course_student_id) {
		courseStudentRepository.saveAndFlush(courseStudent);
		return courseStudent;
	}

	public SlugStudent updateSlugStudent(SlugStudent slugStudent, Long slug_student_id) {
		slugStudentRepository.saveAndFlush(slugStudent);
		cacheService.updateSlugStudentList(slugStudent);
		return slugStudent;
	}

	public Course updateCourse(Course course, Long id) {
		courseRepository.saveAndFlush(course);
		cacheService.updateCourseList(course);
		return course;
	}

	public Slug updateSlug(Slug slug, Long id) {
		slugRepository.saveAndFlush(slug);
		cacheService.updateSlugList(slug);
		return slug;
	}

	public Student updateStudent(Student student, Long id) {
		studentRepository.saveAndFlush(student);
		cacheService.updateStudentList(student);
		return student;
	}

	public void updateSubmissions(Submission submission, String hash) {
		submissionRepository.saveAndFlush(submission);
		cacheService.updateSubmissionList(submission);
	}

}
