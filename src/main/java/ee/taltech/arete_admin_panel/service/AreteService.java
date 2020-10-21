package ee.taltech.arete_admin_panel.service;

import ee.taltech.arete.java.AreteClient;
import ee.taltech.arete.java.TestStatus;
import ee.taltech.arete.java.request.AreteRequest;
import ee.taltech.arete.java.request.hook.AreteTestUpdate;
import ee.taltech.arete.java.request.hook.Commit;
import ee.taltech.arete.java.response.arete.*;
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

	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;
	private final SlugRepository slugRepository;
	private final SubmissionRepository submissionRepository;
	private final JobRepository jobRepository;

	private AreteClient areteClient = new AreteClient(System.getProperty("TESTER_URL", "http://localhost:8098"));
	private Queue<AreteResponse> jobQueue = new LinkedList<>();
	private int antiStuckQueue = 20;
	private Boolean halted = false;

	public AreteService(CacheService cacheService, StudentRepository studentRepository, CourseRepository courseRepository, SlugRepository slugRepository, JobRepository jobRepository, SubmissionRepository submissionRepository) {
		this.cacheService = cacheService;
		this.studentRepository = studentRepository;
		this.courseRepository = courseRepository;
		this.slugRepository = slugRepository;
		this.jobRepository = jobRepository;
		this.submissionRepository = submissionRepository;
	}

	public void enqueueAreteResponse(AreteResponse response) {
		LOG.info("Saving job into DB for user: {} with hash: {} in: {} where queue has {} elements", response.getUniid(), response.getHash(), response.getRoot(), jobQueue.size());
		jobQueue.add(response);
	}

	@Async
	@Scheduled(fixedRate = 100)
	public void asyncRunJob() {

		AreteResponse response = jobQueue.poll();

		if (response != null) {
			if (!halted) {
				try {
					halted = true;
					parseAreteResponse(response);
				} catch (Exception ignored) {
				} finally {
					halted = false;
					antiStuckQueue = 20;
				}
			} else {
				jobQueue.add(response);
				antiStuckQueue -= 1;
			}
		}

		if (antiStuckQueue <= 0) {
			antiStuckQueue = 20;
			halted = false;
		}
	}

	public void parseAreteResponse(AreteResponse response) {
		setDefaultValuesIfNull(response);
		saveSubmission(response);
		saveJob(response);

		if (!response.getFailed()) {
			LOG.debug("getting course");
			CourseEntity course = getCourse(response);

			LOG.debug("getting slug");
			SlugEntity slug = getSlug(response, course);

			LOG.debug("getting student");
			StudentEntity student = getStudent(response, course, slug);

			LOG.debug("update all");
			updateStudentSlugCourse(response, student, slug, course);
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

	private void updateStudentSlugCourse(AreteResponse response, StudentEntity student, SlugEntity slug, CourseEntity course) {

		if (response.getStyle() == 100) {
			slug.setCommitsStyleOK(slug.getCommitsStyleOK() + 1);
			course.setCommitsStyleOK(course.getCommitsStyleOK() + 1);
			student.setCommitsStyleOK(student.getCommitsStyleOK() + 1);
		}

		int newDiagnosticErrors = response.getErrors().size();

		int newTestErrors = 0;
		int newTestPassed = 0;
		int newTestsRan = 0;

		Map<String, Integer> testErrors = new HashMap<>();

		for (TestContext testContext : response.getTestSuites()) {
			for (UnitTest unitTest : testContext.getUnitTests()) {
				newTestsRan += 1;
				if (unitTest.getStatus().equals(TestStatus.FAILED)) {
					newTestErrors += 1;
					if (testErrors.containsKey(unitTest.getExceptionClass())) {
						testErrors.put(unitTest.getExceptionClass(), testErrors.get(unitTest.getExceptionClass()) + 1);
					} else {
						testErrors.put(unitTest.getExceptionClass(), 1);
					}
				}
				if (unitTest.getStatus().equals(TestStatus.PASSED)) {
					newTestPassed += 1;
				}
			}
		}

		slug.setTotalCommits(slug.getTotalCommits() + 1);
		course.setTotalCommits(course.getTotalCommits() + 1);
		student.setTotalCommits(student.getTotalCommits() + 1);

		slug.setTotalDiagnosticErrors(slug.getTotalDiagnosticErrors() + newDiagnosticErrors);
		course.setTotalDiagnosticErrors(course.getTotalDiagnosticErrors() + newDiagnosticErrors);
		student.setTotalDiagnosticErrors(student.getTotalDiagnosticErrors() + newDiagnosticErrors);

		slug.setTotalTestErrors(slug.getTotalTestErrors() + newTestErrors);
		course.setTotalTestErrors(course.getTotalTestErrors() + newTestErrors);
		student.setTotalTestErrors(student.getTotalTestErrors() + newTestErrors);

		slug.setTotalTestsPassed(slug.getTotalTestsPassed() + newTestPassed);
		course.setTotalTestsPassed(course.getTotalTestsPassed() + newTestPassed);
		student.setTotalTestsPassed(student.getTotalTestsPassed() + newTestPassed);

		slug.setTotalTestsRan(slug.getTotalTestsRan() + newTestsRan);
		course.setTotalTestsRan(course.getTotalTestsRan() + newTestsRan);
		student.setTotalTestsRan(student.getTotalTestsRan() + newTestsRan);

		student.getTimestamps().add(response.getTimestamp());
		student.setLastTested(response.getTimestamp());


		student.setDifferentCourses(student.getCourses().size());
		student.setDifferentSlugs(student.getSlugs().size());

		updateCourse(course, course.getId());
		updateSlug(slug, slug.getId());
		updateStudent(student, student.getId());

	}

	private SlugEntity getSlug(AreteResponse response, CourseEntity course) {
		SlugEntity slug;
		Optional<SlugEntity> optionalSlug = slugRepository.findByCourseUrlAndName(course.getGitUrl(), response.getSlug());
		slug = optionalSlug.orElseGet(() -> SlugEntity.builder()
				.courseUrl(course.getGitUrl())
				.name(response.getSlug())
				.build());

		return slug;
	}

	private CourseEntity getCourse(AreteResponse response) {
		CourseEntity course;
		Optional<CourseEntity> optionalCourse = courseRepository.findByGitUrl(response.getGitTestRepo());
		course = optionalCourse.orElseGet(() -> CourseEntity.builder()
				.gitUrl(response.getGitTestRepo())
				.name(response.getRoot())
				.build());

		return course;
	}

	private StudentEntity getStudent(AreteResponse response, CourseEntity course, SlugEntity slug) {
		StudentEntity student;
		Optional<StudentEntity> optionalStudent = studentRepository.findByUniid(response.getUniid());
		student = optionalStudent.orElseGet(() -> StudentEntity.builder()
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

	private void saveJob(AreteResponse response) {
		JobEntity job = JobEntity.builder()
				.output(response.getOutput().replace("\n", "<br>"))
				.consoleOutput(response.getConsoleOutputs().stream()
						.map(ConsoleOutput::getContent).collect(Collectors.joining())
						.replace("\n", "<br>"))
				.testSuites(response.getTestSuites().stream()
						.map(x -> TestContextEntity.builder()
								.endDate(x.getEndDate())
								.file(x.getFile())
								.grade(x.getGrade())
								.name(x.getName())
								.passedCount(x.getPassedCount())
								.startDate(x.getStartDate())
								.weight(x.getWeight())
								.unitTests(
										x.getUnitTests().stream()
												.map(y -> UnitTestEntity.builder()
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

		LOG.info("Saving job");
		jobRepository.save(job);
	}

	public SubmissionEntity saveSubmission(AreteResponse response) {
		SubmissionEntity submission = SubmissionEntity.builder()
				.uniid(response.getUniid())
				.slug(response.getSlug())
				.hash(response.getHash())
				.testingPlatform(response.getTestingPlatform())
				.root(response.getRoot())
				.timestamp(response.getTimestamp())
				.gitStudentRepo(response.getGitStudentRepo())
				.gitTestSource(response.getGitTestRepo())
				.failed(response.getFailed())
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
		Commit latest = update.getCommits().get(0);

		Set<String> slugs = new HashSet<>();
		slugs.addAll(latest.getAdded());
		slugs.addAll(latest.getModified());

		AreteRequest request = AreteRequest.builder()
				.eMail(latest.getAuthor().getEmail())
				.uniid(latest.getAuthor().getName())
				.gitStudentRepo(update.getProject().getUrl())
				.gitTestRepo(testRepository)
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

	public CourseEntity updateCourse(CourseEntity course, Long id) {
		LOG.info("Updating course cache: {}", id);
		courseRepository.save(course);
		cacheService.updateCourseList(course);
		return course;
	}

	public SlugEntity updateSlug(SlugEntity slug, Long id) {
		LOG.info("Updating slug cache: {}", id);
		slugRepository.save(slug);
		cacheService.updateSlugList(slug);
		return slug;
	}

	public StudentEntity updateStudent(StudentEntity student, Long id) {
		LOG.info("Updating student cache: {}", id);
		studentRepository.save(student);
		cacheService.updateStudentList(student);
		return student;
	}

	public void updateSubmissions(SubmissionEntity submission, String hash) {
		LOG.info("Updating submission cache: {}", hash);
		submissionRepository.save(submission);
		cacheService.updateSubmissionList(submission);
	}

}
