package ee.taltech.arete_admin_panel.repository;

import arete.java.response.AreteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete_admin_panel.pojo.abi.users.UserPostDto;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Slf4j
class LoadDatabase {

    @Bean
    CommandLineRunner initUserDatabase(UserService userService) {
        return args -> {
            if (userService.getAllUsers().stream().noneMatch(x -> x.getUsername().equals("admin"))) {
                userService.addSuperUser(new UserPostDto("admin", "admin"));
            }
        };
    }

    @Bean
    CommandLineRunner initSubmissionAndJobDatabase(AreteService areteService) {
        return args -> {

            for (int i = 0; i < 10; i++) {
                System.out.println(i);
                String answer = Files.readString(Paths.get("C:\\Users\\envomp\\IdeaProjects\\arete_admin_panel\\src\\main\\java\\ee\\taltech\\arete_admin_panel\\repository\\areteResponse.json"));
                ObjectMapper objectMapper = new ObjectMapper();
                areteService.parseAreteResponse(objectMapper.readValue(answer, AreteResponse.class));

            }
        };
    }

    private static String getRandomHash() {
        return RandomStringUtils.random(64, true, true).toLowerCase();
    }

}
