package ee.taltech.arete_admin_panel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.domain.Course;
import ee.taltech.arete_admin_panel.domain.Slug;
import ee.taltech.arete_admin_panel.domain.Student;
import ee.taltech.arete_admin_panel.domain.Submission;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentDto;
import ee.taltech.arete_admin_panel.repository.CourseRepository;
import ee.taltech.arete_admin_panel.repository.SlugRepository;
import ee.taltech.arete_admin_panel.repository.StudentRepository;
import ee.taltech.arete_admin_panel.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CacheService {

	private final int CACHE_MAX_SIZE = 10000;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Logger logger;

	Map<Long, Submission> submissionCache = Collections
			.synchronizedMap(new LinkedHashMap<>(CACHE_MAX_SIZE, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});

	Map<Long, StudentDto> studentCache = Collections
			.synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});

	Map<Long, CourseDto> courseCache = Collections
			.synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});

	Map<Long, SlugDto> slugCache = Collections
			.synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});


	public CacheService(StudentRepository studentRepository,
						CourseRepository courseRepository,
						SlugRepository slugRepository,
						SubmissionRepository submissionRepository, Logger logger) {
		this.logger = logger;

		submissionRepository.findTop10000ByOrderByIdDesc().forEach(x -> submissionCache.put(x.getId(), x));
		this.logger.info("Loaded submissions to cache");

		studentRepository.findTop10000ByOrderByIdDesc()
				.forEach(x -> studentCache.put(x.getId(), objectMapper.convertValue(x, StudentDto.class)));
		this.logger.info("Loaded students to cache");

		courseRepository.findAll()
				.forEach(x -> courseCache.put(x.getId(), objectMapper.convertValue(x, CourseDto.class)));
		this.logger.info("Loaded courses to cache");

		slugRepository.findAll()
				.forEach(x -> slugCache.put(x.getId(), objectMapper.convertValue(x, SlugDto.class)));
		this.logger.info("Loaded slugs to cache");
	}

	public void updateSubmissionList(Submission submission) {
		logger.debug("Update submission cache");
		submissionCache.put(submission.getId(), submission);

	}

	public void updateStudentList(Student student) {
		logger.debug("Update student cache");
		StudentDto studentDto = objectMapper.convertValue(student, StudentDto.class);
		studentCache.put(studentDto.getId(), studentDto);
	}

	public void updateCourseList(Course course) {
		logger.debug("Update course cache");
		CourseDto courseDto = objectMapper.convertValue(course, CourseDto.class);
		courseCache.put(courseDto.getId(), courseDto);
	}

	public void updateSlugList(Slug slug) {
		logger.debug("Update slug cache");
		SlugDto slugDto = objectMapper.convertValue(slug, SlugDto.class);
		slugCache.put(slugDto.getId(), slugDto);
	}

	public Collection<Submission> getSubmissionList() {
		logger.info("Reading all submissions from cache");
		return submissionCache.values();
	}

	public Collection<StudentDto> getStudentList() {
		logger.info("Reading all students from cache");
		return studentCache.values();
	}

	public Collection<CourseDto> getCourseList() {
		logger.info("Reading all courses from cache");
		return courseCache.values();
	}

	public Collection<SlugDto> getSlugList() {
		logger.info("Reading all slugs from cache");
		return slugCache.values();
	}
}
