package ee.taltech.arete_admin_panel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.domain.Course;
import ee.taltech.arete_admin_panel.domain.Slug;
import ee.taltech.arete_admin_panel.domain.Student;
import ee.taltech.arete_admin_panel.domain.Submission;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentTableDto;
import ee.taltech.arete_admin_panel.repository.CourseTableDtoRepository;
import ee.taltech.arete_admin_panel.repository.SlugTableDtoRepository;
import ee.taltech.arete_admin_panel.repository.StudentTableDtoRepository;
import ee.taltech.arete_admin_panel.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CacheService {

	private final int CACHE_MAX_SIZE = 10000;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Logger logger;
	private final StudentTableDtoRepository studentTableDtoRepository;

	Map<Long, Submission> submissionCache = Collections
			.synchronizedMap(new LinkedHashMap<>(CACHE_MAX_SIZE, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});

	Map<Long, StudentTableDto> studentCache = Collections
			.synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});

	Map<Long, CourseTableDto> courseCache = Collections
			.synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});

	Map<Long, SlugTableDto> slugCache = Collections
			.synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
				@Override
				protected boolean removeEldestEntry(final Map.Entry eldest) {
					return size() > CACHE_MAX_SIZE;
				}
			});


	public CacheService(StudentTableDtoRepository studentTableDtoRepository,
						CourseTableDtoRepository courseTableDtoRepository,
						SlugTableDtoRepository slugTableDtoRepository,
						SubmissionRepository submissionRepository, Logger logger) {
		this.studentTableDtoRepository = studentTableDtoRepository;
		this.logger = logger;

		submissionRepository.findTop10000ByOrderByIdDesc().forEach(x -> submissionCache.put(x.getId(), x));
		this.logger.info("Loaded submissions to cache");
		getAllStudents().forEach(x -> studentCache.put(x.getId(), x));
		this.logger.info("Loaded students to cache");
		courseTableDtoRepository.findAll().forEach(x -> courseCache.put(x.getId(), x));
		this.logger.info("Loaded courses to cache");
		slugTableDtoRepository.findAll().forEach(x -> slugCache.put(x.getId(), x));
		this.logger.info("Loaded slugs to cache");
	}

	private List<StudentTableDto> getAllStudents() {
		logger.info("Reading all students from cache");
		return new ArrayList<>(studentTableDtoRepository.findAll());
	}

	public void updateSubmissionList(Submission submission) {
		logger.debug("Update submission cache");
		submissionCache.put(submission.getId(), submission);

	}

	public void updateStudentList(Student student) {
		logger.debug("Update student cache");
		StudentTableDto studentTableDto = objectMapper.convertValue(student, StudentTableDto.class);
		studentCache.put(studentTableDto.getId(), studentTableDto);
	}

	public void updateCourseList(Course course) {
		logger.debug("Update course cache");
		CourseTableDto courseTableDto = objectMapper.convertValue(course, CourseTableDto.class);
		courseCache.put(courseTableDto.getId(), courseTableDto);
	}

	public void updateSlugList(Slug slug) {
		logger.debug("Update slug cache");
		SlugTableDto slugTableDto = objectMapper.convertValue(slug, SlugTableDto.class);
		slugCache.put(slugTableDto.getId(), slugTableDto);
	}

	public Collection<Submission> getSubmissionList() {
		logger.info("Reading all submissions from cache");
		return submissionCache.values();
	}

	public Collection<StudentTableDto> getStudentList() {
		logger.info("Reading all students from cache");
		return studentCache.values();
	}

	public Collection<CourseTableDto> getCourseList() {
		logger.info("Reading all courses from cache");
		return courseCache.values();
	}

	public Collection<SlugTableDto> getSlugList() {
		logger.info("Reading all slugs from cache");
		return slugCache.values();
	}
}
