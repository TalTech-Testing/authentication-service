package ee.taltech.arete_admin_panel.service;

import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete_admin_panel.AreteAdminPanelApplication;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase
@SpringBootTest(
        classes = AreteAdminPanelApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class AreteServiceTest {

    @Autowired
    private AreteService areteService;

    @Autowired
    private CacheService cacheService;

    @Test
    @SneakyThrows
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

        AreteResponseDTO responseDTO2 = AreteResponseDTO.builder()
                .uniid("envomp")
                .hash("hash2")
                .gitTestRepo("git2")
                .consoleOutputs("console2")
                .dockerExtra("extra2")
                .slug("slug2")
                .build();
        areteService.parseAreteResponseDTO(responseDTO2);

        TimeUnit.SECONDS.sleep(1);

        Assertions.assertEquals(cacheService.getStudent("envomp").get().getId(), Objects.hash("envomp"));
        Assertions.assertEquals(cacheService.getStudent("envomp").get().getTotalCommits(), 2);

        Assertions.assertEquals(cacheService.getCourse("git").get().getId(), Objects.hash("git"));
        Assertions.assertEquals(cacheService.getCourse("git").get().getTotalCommits(), 1);

        Assertions.assertEquals(cacheService.getSlug("slug", "git").get().getId(), Objects.hash("slug", "git"));
        Assertions.assertEquals(cacheService.getSlug("slug", "git").get().getTotalCommits(), 1);
    }
}