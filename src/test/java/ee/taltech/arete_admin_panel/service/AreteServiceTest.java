package ee.taltech.arete_admin_panel.service;

import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete_admin_panel.AreteAdminPanelApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase
@SpringBootTest(
        classes = AreteAdminPanelApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class AreteServiceTest {

    @Autowired
    private AreteService areteService;

    @Test
    void parseAreteResponseDTO() {
        AreteResponseDTO responseDTO = AreteResponseDTO.builder()
                .uniid("envomp")
                .hash("hash")
                .gitTestRepo("git")
                .consoleOutputs("console")
                .dockerExtra("extra")
                .slug("slug")
                .build();
        areteService.parseAreteResponseDTO(responseDTO);

        responseDTO = AreteResponseDTO.builder()
                .uniid("envomp")
                .hash("hash2")
                .gitTestRepo("git2")
                .consoleOutputs("console2")
                .dockerExtra("extra2")
                .slug("slug2")
                .build();
        areteService.parseAreteResponseDTO(responseDTO);
    }
}