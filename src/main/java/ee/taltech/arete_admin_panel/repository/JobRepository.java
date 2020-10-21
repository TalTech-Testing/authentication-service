package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {

    List<JobEntity> findByHash(@Param("hash") String hash);

    List<JobEntity> findTop10ByHashOrderByIdDesc(@Param("hash") String hash);

}
