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
	private final SlugStudentRepository slugStudentRepository;
	private final CourseRepository courseRepository;
	private final SlugRepository slugRepository;
	private final SubmissionRepository submissionRepository;

	HashMap<Long, Submission> submissionCache = new HashMap<>();
	HashMap<Long, StudentTableDto> studentCache = new HashMap<>();
	HashMap<Long, CourseTableDto> courseCache = new HashMap<>();
	HashMap<Long, SlugTableDto> slugCache = new HashMap<>();
	HashMap<Long, SlugStudent> slugStudentCache = new HashMap<>();


	public void updateSubmissionList(Submission submission) {
		submissionCache.put(submission.getId(), submission);

	}

	public void updateStudentList(Student student) {
		StudentTableDto studentTableDto = objectMapper.convertValue(student, StudentTableDto.class);
		studentCache.put(studentTableDto.getId(), studentTableDto);
	}

	public void updateCourseList(Course course) {
		CourseTableDto courseTableDto = objectMapper.convertValue(course, CourseTableDto.class);
		courseCache.put(courseTableDto.getId(), courseTableDto);
	}

	public void updateSlugList(Slug slug) {
		SlugTableDto slugTableDto = objectMapper.convertValue(slug, SlugTableDto.class);
		slugCache.put(slugTableDto.getId(), slugTableDto);
	}

	public void updateSlugStudentList(SlugStudent slugStudent) {
		slugStudentCache.put(slugStudent.getId(), slugStudent);
	}

	public CacheService(StudentRepository studentRepository, SlugStudentRepository slugStudentRepository, CourseRepository courseRepository, SlugRepository slugRepository, SubmissionRepository submissionRepository) {
		this.studentRepository = studentRepository;
		this.slugStudentRepository = slugStudentRepository;
		this.courseRepository = courseRepository;
		this.slugRepository = slugRepository;
		this.submissionRepository = submissionRepository;

		submissionRepository.findTop100ByOrderByIdDesc().forEach(x -> submissionCache.put(x.getId(), x));
		slugStudentRepository.findTop500ByOrderByIdDesc().forEach(x -> slugStudentCache.put(x.getId(), x));
		getAllStudents().forEach(x -> studentCache.put(x.getId(), x));
		courseRepository.findTop500ByOrderByIdDesc().stream().map(x -> objectMapper.convertValue(x, CourseTableDto.class)).collect(Collectors.toList()).forEach(x -> courseCache.put(x.getId(), x));
		slugRepository.findTop500ByOrderByIdDesc().stream().map(x -> objectMapper.convertValue(x, SlugTableDto.class)).collect(Collectors.toList()).forEach(x -> slugCache.put(x.getId(), x));
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
		return studentRepository
				.findTop1000ByOrderByIdDesc()
				.stream().map(x -> objectMapper.convertValue(x, StudentTableDto.class))
				.map(x -> calculateFields(x, slugStudentCache.values()))
				.collect(Collectors.toList());
	}

	private StudentTableDto calculateFields(StudentTableDto dto, Collection<SlugStudent> slugStudents) {
		List<Double> grades = new ArrayList<>();

		for (SlugStudent slugStudent : slugStudents) {
			if (slugStudent.getUniid().equals(dto.getUniid())) {
				grades.add(slugStudent.getHighestPercent());
			}
		}

		dto.setAverageGrade(grades.stream().flatMapToDouble(DoubleStream::of).average().orElse(0));
		dto.setMedianGrade(grades.size() > 0 ? grades.get(grades.size() / 2) : 0.0);
		return dto;
	}
}
