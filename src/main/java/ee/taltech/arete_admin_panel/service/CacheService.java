package ee.taltech.arete_admin_panel.service;

import ee.taltech.arete_admin_panel.domain.Course;
import ee.taltech.arete_admin_panel.domain.Slug;
import ee.taltech.arete_admin_panel.domain.Student;
import ee.taltech.arete_admin_panel.domain.Submission;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CacheService {

    private final int CACHE_MAX_SIZE = 10000;
    private final Logger logger;

    private final ConcurrentLinkedQueue<Submission> toProcess = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    private final Map<Integer, Submission> submissionCache = Collections
            .synchronizedMap(new LinkedHashMap<>(CACHE_MAX_SIZE, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > CACHE_MAX_SIZE;
                }
            });

    private final Map<Integer, Student> studentCache = Collections
            .synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > CACHE_MAX_SIZE;
                }
            });

    private final Map<Integer, Course> courseCache = Collections
            .synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > CACHE_MAX_SIZE;
                }
            });

    private final Map<Integer, Slug> slugCache = Collections
            .synchronizedMap(new LinkedHashMap<>(10000, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > CACHE_MAX_SIZE;
                }
            });

    public CacheService(Logger logger) {
        this.logger = logger;
    }

    @SneakyThrows
    public void enqueueSubmission(Submission submission) {
        logger.debug("Enqueueing submission {}", submission.getHash());
        toProcess.add(submission);
    }

    @Async
    @Scheduled(fixedRate = 100)
    public void process() {
        if (isProcessing.get()) {
            return;
        }
        isProcessing.set(true);

        try {
            while (toProcess.peek() != null) {
                Submission submission = toProcess.poll();
                logger.info("Processing submission {}", submission.getHash());

                Course course = getCourse(submission);
                Slug slug = getSlug(submission);
                Student student = getStudent(submission);

                updateStudentSlugCourse(submission, student, slug, course);

                updateSubmissionCache(submission);
                updateCourseCache(course);
                updateSlugCache(slug);
                updateStudentCache(student);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isProcessing.set(false);
    }

    private Course getCourse(Submission submission) {
        Optional<Course> optionalCourse = getCourse(submission.getGitTestSource());
        return optionalCourse.orElseGet(() -> Course.builder()
                .id(Objects.hash(submission.getGitTestSource()))
                .gitUrl(submission.getGitTestSource())
                .name(submission.getRoot())
                .build());
    }

    private Slug getSlug(Submission submission) {
        Optional<Slug> optionalSlug = getSlug(submission.getSlug(), submission.getGitTestSource());
        return optionalSlug.orElseGet(() -> Slug.builder()
                .id(Objects.hash(submission.getSlug(), submission.getGitTestSource()))
                .courseUrl(submission.getGitTestSource())
                .name(submission.getSlug())
                .build());
    }

    private Student getStudent(Submission submission) {
        Optional<Student> optionalStudent = getStudent(submission.getUniid());
        Student student = optionalStudent.orElseGet(() -> Student.builder()
                .id(Objects.hash(submission.getUniid()))
                .uniid(submission.getUniid())
                .firstTested(submission.getTimestamp())
                .lastTested(submission.getTimestamp())
                .build());

        if (student.getGitRepo() == null && submission.getGitStudentRepo() != null) {
            student.setGitRepo(submission.getGitTestSource());
        }

        student.getCourses().add(submission.getGitTestSource());
        student.getSlugs().add(submission.getSlug());
        return student;
    }

    private void updateStudentSlugCourse(Submission submission, Student student, Slug slug, Course course) {

        if (submission.getStyle() == 100) {
            slug.setCommitsStyleOK(slug.getCommitsStyleOK() + 1);
            course.setCommitsStyleOK(course.getCommitsStyleOK() + 1);
            student.setCommitsStyleOK(student.getCommitsStyleOK() + 1);
        }

        int newDiagnosticErrors = submission.getDiagnosticErrors();

        int newTestErrors = 0;
        int newTestPassed = 0;
        int newTestsRan = 0;

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

        student.getTimestamps().add(submission.getTimestamp());
        student.setLastTested(submission.getTimestamp());

        student.setDifferentCourses(student.getCourses().size());
        student.setDifferentSlugs(student.getSlugs().size());
    }

    // update

    public void updateStudentCache(Student student) {
        logger.debug("Update student cache");
        studentCache.put(Objects.hash(student.getUniid()), student);
    }

    public void updateSlugCache(Slug slug) {
        logger.debug("Update slug cache");
        slugCache.put(Objects.hash(slug.getName(), slug.getCourseUrl()), slug);
    }

    public void updateSubmissionCache(Submission submission) {
        logger.debug("Update submission cache");
        submissionCache.put(Objects.hash(submission.getId()), submission);
    }

    public void updateCourseCache(Course course) {
        logger.debug("Update course cache");
        courseCache.put(Objects.hash(course.getGitUrl()), course);
    }

    // singleton by values

    public Optional<Course> getCourse(String gitUrl) {
        logger.info("Getting course by url {}", gitUrl);
        return Optional.ofNullable(courseCache.getOrDefault(Objects.hash(gitUrl), null));
    }

    public Optional<Slug> getSlug(String name, String gitUrl) {
        logger.info("Getting slug by name {} and url {}", name, gitUrl);
        return Optional.ofNullable(slugCache.getOrDefault(Objects.hash(name, gitUrl), null));
    }

    public Optional<Student> getStudent(String uniid) {
        logger.info("Getting student by uniid {}", uniid);
        return Optional.ofNullable(studentCache.getOrDefault(Objects.hash(uniid), null));
    }

    // collection

    public Collection<Submission> getSubmissionList() {
        logger.info("Reading all submissions from cache");
        return submissionCache.values();
    }

    public Collection<Student> getStudentList() {
        logger.info("Reading all students from cache");
        return studentCache.values();
    }

    public Collection<Course> getCourseList() {
        logger.info("Reading all courses from cache");
        return courseCache.values();
    }

    public Collection<Slug> getSlugList() {
        logger.info("Reading all slugs from cache");
        return slugCache.values();
    }

    // singleton by key

    public Optional<Student> getStudent(Integer id) {
        return Optional.ofNullable(studentCache.getOrDefault(id, null));
    }

    public Optional<Slug> getSlug(Integer id) {
        return Optional.ofNullable(slugCache.getOrDefault(id, null));
    }

    public Optional<Course> getCourse(Integer id) {
        return Optional.ofNullable(courseCache.getOrDefault(id, null));
    }

    public Optional<Submission> getSubmission(Integer id) {
        return Optional.ofNullable(submissionCache.getOrDefault(id, null));
    }
}
