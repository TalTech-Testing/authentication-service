package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.pojo.abi.users.student.StudentTableDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StudentTableDtoRepository extends JpaRepository<StudentTableDto, Long> {
}