package ee.taltech.arete_admin_panel;

import ee.taltech.arete_admin_panel.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = AreteAdminPanelApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional
class AreteAdminPanelApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
        assert true;
    }

}
