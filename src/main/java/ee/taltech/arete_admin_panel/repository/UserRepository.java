package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(@Param("username") String username);

    void deleteByUsername(@Param("username") String username);
}

