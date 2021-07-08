package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.service.CacheService;
import ee.taltech.arete_admin_panel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabaseAndCache(UserService userService,
                                           SubmissionRepository submissionRepository,
                                           CacheService cacheService) {
        return args -> {
            if (userService.getUser("admin").isEmpty()) {
                userService.addSuperUser("admin", System.getProperty("ADMIN_PASS", "admin"));
            }

            submissionRepository.findAll().forEach(cacheService::enqueueSubmission);
        };
    }
}
