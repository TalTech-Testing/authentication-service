package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUniid(@Param("uniid") String uniid);

    List<Student> findTop500ByOrderByIdDesc();

}