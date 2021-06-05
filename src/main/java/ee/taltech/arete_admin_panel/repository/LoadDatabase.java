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
	CommandLineRunner initDatabaseAndCache(UserService userService, SubmissionRepository submissionRepository, CacheService cacheService) {
		return args -> {
			if (userService.getUser("admin").isEmpty()) {
				if (System.getProperty("ADMIN_PASS", "<3").equals("false")) {
					userService.addSuperUser("admin", "b0f8425ea1a133e0cd689b0bde8e8a8738c8e6b9120d8b68ef16289a341298e48c5c448220e25a3d3402302097f5ff82339cc3e9ffc50b6c6f15546b6d81a33e", "9a519963bc8f441eb73c71894edfb65");
				} else { // admin
					userService.addSuperUser("admin", System.getProperty("ADMIN_PASS", "admin"));
				}
			}

			submissionRepository.findAll().forEach(cacheService::enqueueSubmission);
		};
	}

}
