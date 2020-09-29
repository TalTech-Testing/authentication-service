package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findTop500ByOrderByIdDesc();
	List<Submission> findTop100ByOrderByIdDesc();
}
