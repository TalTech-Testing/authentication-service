package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

}
