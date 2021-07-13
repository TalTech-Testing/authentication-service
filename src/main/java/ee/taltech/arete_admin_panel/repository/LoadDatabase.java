package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.service.CacheService;
import ee.taltech.arete_admin_panel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabaseAndCache(UserService userService,
                                           SubmissionRepository submissionRepository,
                                           CacheService cacheService,
                                           Logger logger) {
        return args -> {
            if (userService.getUser("admin").isEmpty()) {
                String password = System.getenv("ADMIN_PASS");
                if (password == null) {
                    logger.warn("ADMIN_PASS env parameter is not set. Defaulting to admin. This is not recommended on production environment!");
                    password = "admin";
                }
                userService.addSuperUser("admin", password);
            }

            submissionRepository.findAll().forEach(cacheService::enqueueSubmission);
        };
    }
}
