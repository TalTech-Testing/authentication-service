package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Slug;
import ee.taltech.arete_admin_panel.domain.Student;
import ee.taltech.arete_admin_panel.domain.StudentDataSlug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface StudentDataSlugRepository extends JpaRepository<StudentDataSlug, Long> {

    Optional<StudentDataSlug> findByStudentAndSlug(@Param("student") Student student, @Param("slug") Slug slug);

}