package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Job;
import ee.taltech.arete_admin_panel.domain.Slug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlugRepository extends JpaRepository<Slug, Long> {

    Optional<Slug> findByCourseUrlAndName(@Param("courseUrl") String courseUrl, @Param("name") String name);

    List<Slug> findAllTop500ByIdExists();

}