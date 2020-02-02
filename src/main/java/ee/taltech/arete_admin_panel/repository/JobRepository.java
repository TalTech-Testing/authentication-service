package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    ArrayList<Job> findByHash(@Param("hash") String hash);

}
