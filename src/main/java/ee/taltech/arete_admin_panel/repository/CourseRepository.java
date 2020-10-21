package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

	Optional<Course> findByGitUrl(@Param("gitUrl") String gitUrl);

}