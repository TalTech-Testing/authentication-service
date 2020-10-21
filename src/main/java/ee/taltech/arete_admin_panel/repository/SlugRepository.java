package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.SlugEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlugRepository extends JpaRepository<SlugEntity, Long> {

    Optional<SlugEntity> findByCourseUrlAndName(@Param("courseUrl") String courseUrl, @Param("name") String name);

    List<SlugEntity> findAllByName(@Param("name") String name);

	List<SlugEntity> findTop1000ByOrderByIdDesc();

    List<SlugEntity> findTop100ByOrderByIdDesc();

}