package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Course;
import ee.taltech.arete_admin_panel.domain.Slug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlugRepository extends JpaRepository<Slug, Long> {

    Optional<Slug> findByCourseAndName(@Param("course") Course course, @Param("name") String name);

}