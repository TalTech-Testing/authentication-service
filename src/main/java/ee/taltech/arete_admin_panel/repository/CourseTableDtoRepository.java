package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.pojo.abi.users.course.CourseTableDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTableDtoRepository extends JpaRepository<CourseTableDto, Long> {
}
