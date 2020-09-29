package ee.taltech.arete_admin_panel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.domain.*;
import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.slug.SlugTableDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentTableDto;
import ee.taltech.arete_admin_panel.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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

	HashMap<Long, Submission> submissionCache = new HashMap<>();
	HashMap<Long, StudentTableDto> studentCache = new HashMap<>();
	HashMap<Long, CourseTableDto> courseCache = new HashMap<>();
	HashMap<Long, SlugTableDto> slugCache = new HashMap<>();


	public void updateSubmissionList(Submission submission) {
		LOG.debug("Update submission cache");
		submissionCache.put(submission.getId(), submission);

	}

	public void updateStudentList(Student student) {
		LOG.debug("Update student cache");
		StudentTableDto studentTableDto = objectMapper.convertValue(student, StudentTableDto.class);
		studentCache.put(studentTableDto.getId(), studentTableDto);
	}

	public void updateCourseList(Course course) {
		LOG.debug("Update course cache");
		CourseTableDto courseTableDto = objectMapper.convertValue(course, CourseTableDto.class);
		courseCache.put(courseTableDto.getId(), courseTableDto);
	}

	public void updateSlugList(Slug slug) {
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

		submissionRepository.findTop100ByOrderByIdDesc().forEach(x -> submissionCache.put(x.getId(), x));
		LOG.info("Loaded submissions to cache");
		getAllStudents().forEach(x -> studentCache.put(x.getId(), x));
		LOG.info("Loaded students to cache");
		courseTableDtoRepository.findAll().forEach(x -> courseCache.put(x.getId(), x));
		LOG.info("Loaded courses to cache");
		slugTableDtoRepository.findAll().forEach(x -> slugCache.put(x.getId(), x));
		LOG.info("Loaded slugs to cache");
	}

	public Collection<Submission> getSubmissionList() {
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

	public List<StudentTableDto> getAllStudents() {
		LOG.info("Reading all students from cache");
		return new ArrayList<>(studentTableDtoRepository.findAll());
	}
}
