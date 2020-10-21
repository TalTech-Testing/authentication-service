package ee.taltech.arete_admin_panel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.domain.CourseEntity;
import ee.taltech.arete_admin_panel.domain.SlugEntity;
import ee.taltech.arete_admin_panel.domain.StudentEntity;
import ee.taltech.arete_admin_panel.domain.SubmissionEntity;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentTableDto;
import ee.taltech.arete_admin_panel.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CacheService {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final StudentRepository studentRepository;
	private final StudentTableDtoRepository studentTableDtoRepository;
	private final CourseRepository courseRepository;
	private final CourseTableDtoRepository courseTableDtoRepository;
	private final SlugRepository slugRepository;
	private final SlugTableDtoRepository slugTableDtoRepository;
	private final SubmissionRepository submissionRepository;

	Map<Long, SubmissionEntity> submissionCache = new LinkedHashMap<>(10000, 0.75f, false);
	Map<Long, StudentTableDto> studentCache = new LinkedHashMap<>(10000, 0.75f, false);
	Map<Long, CourseTableDto> courseCache = new LinkedHashMap<>(10000, 0.75f, false);
	Map<Long, SlugTableDto> slugCache = new LinkedHashMap<>(10000, 0.75f, false);


	public void updateSubmissionList(SubmissionEntity submission) {
		LOG.debug("Update submission cache");
		submissionCache.put(submission.getId(), submission);

	}

	public void updateStudentList(StudentEntity student) {
		LOG.debug("Update student cache");
		StudentTableDto studentTableDto = objectMapper.convertValue(student, StudentTableDto.class);
		studentCache.put(studentTableDto.getId(), studentTableDto);
	}

	public void updateCourseList(CourseEntity course) {
		LOG.debug("Update course cache");
		CourseTableDto courseTableDto = objectMapper.convertValue(course, CourseTableDto.class);
		courseCache.put(courseTableDto.getId(), courseTableDto);
	}

	public void updateSlugList(SlugEntity slug) {
		LOG.debug("Update slug cache");
		SlugTableDto slugTableDto = objectMapper.convertValue(slug, SlugTableDto.class);
		slugCache.put(slugTableDto.getId(), slugTableDto);
	}

	public CacheService(StudentRepository studentRepository, StudentTableDtoRepository studentTableDtoRepository, CourseRepository courseRepository, CourseTableDtoRepository courseTableDtoRepository, SlugRepository slugRepository, SlugTableDtoRepository slugTableDtoRepository, SubmissionRepository submissionRepository) {
		this.studentRepository = studentRepository;
		this.studentTableDtoRepository = studentTableDtoRepository;
		this.courseRepository = courseRepository;
		this.courseTableDtoRepository = courseTableDtoRepository;
		this.slugRepository = slugRepository;
		this.slugTableDtoRepository = slugTableDtoRepository;
		this.submissionRepository = submissionRepository;

		submissionRepository.findTop10000ByOrderByIdDesc().forEach(x -> submissionCache.put(x.getId(), x));
		LOG.info("Loaded submissions to cache");
		getAllStudents().forEach(x -> studentCache.put(x.getId(), x));
		LOG.info("Loaded students to cache");
		courseTableDtoRepository.findAll().forEach(x -> courseCache.put(x.getId(), x));
		LOG.info("Loaded courses to cache");
		slugTableDtoRepository.findAll().forEach(x -> slugCache.put(x.getId(), x));
		LOG.info("Loaded slugs to cache");
	}

	public Collection<SubmissionEntity> getSubmissionList() {
		LOG.info("Reading all submissions from cache");
		return submissionCache.values();
	}

	public Collection<StudentTableDto> getStudentList() {
		LOG.info("Reading all students from cache");
		return studentCache.values();
	}

	public Collection<CourseTableDto> getCourseList() {
		LOG.info("Reading all courses from cache");
		return courseCache.values();
	}

	public Collection<SlugTableDto> getSlugList() {
		LOG.info("Reading all slugs from cache");
		return slugCache.values();
	}

	private List<StudentTableDto> getAllStudents() {
		LOG.info("Reading all students from cache");
		return new ArrayList<>(studentTableDtoRepository.findAll());
	}
}
