package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findAllTop500ByIdExists();

}
