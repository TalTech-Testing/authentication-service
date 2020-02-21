package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Slug;
import ee.taltech.arete_admin_panel.domain.SlugStudent;
import ee.taltech.arete_admin_panel.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface SlugStudentRepository extends JpaRepository<SlugStudent, Long> {

    Optional<SlugStudent> findByStudentAndSlug(@Param("student") Student student, @Param("slug") Slug slug);

    List<SlugStudent> findAllTop500ByIdExists();

}