package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {

    List<SubmissionEntity> findTop500ByOrderByIdDesc();
	List<SubmissionEntity> findTop10000ByOrderByIdDesc();
}
