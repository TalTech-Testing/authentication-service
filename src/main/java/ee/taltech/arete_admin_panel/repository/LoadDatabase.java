package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadDatabase {

	private static String getRandomHash() {
		return RandomStringUtils.random(64, true, true).toLowerCase();
	}

//    @Bean
//    CommandLineRunner initSubmissionAndJobDatabase(AreteService areteService) {
//        return args -> {
//
//            for (int i = 0; i < 10; i++) {
//                String answer = Files.readString(Paths.get("C:\\Users\\envomp\\IdeaProjects\\arete-ui-back\\src\\main\\java\\ee\\taltech\\arete_admin_panel\\repository\\areteResponse.json"));
//                ObjectMapper objectMapper = new ObjectMapper();
//                areteService.parseAreteResponse(objectMapper.readValue(answer, AreteResponse.class));
//
//            }
//        };
//    }
//
//    @Bean
//    CommandLineRunner initSubmissionAndJobDatabase(CourseStudentRepository courseStudentRepository, StudentRepository studentRepository, SlugStudentRepository slugStudentRepository) {
//        return args -> {
//
//            for (SlugStudent slugStudent : slugStudentRepository.findAll()) {
//
//                slugStudent.setUniid(slugStudent.getStudent().getUniid());
//                slugStudent.getStudent().getTimestamps().addAll(slugStudent.getTimestamps());
//                slugStudentRepository.saveAndFlush(slugStudent);
//
//            }
//
//        };
//    }

	@Bean
	CommandLineRunner initUserDatabase(UserService userService) {
		return args -> {
			if (userService.getAllUsers().stream().noneMatch(x -> x.getUsername().equals("admin"))) {
				if (System.getProperty("ADMIN_PASS", "<3").equals("false")) {
					userService.addSuperUser("admin", "b0f8425ea1a133e0cd689b0bde8e8a8738c8e6b9120d8b68ef16289a341298e48c5c448220e25a3d3402302097f5ff82339cc3e9ffc50b6c6f15546b6d81a33e", "9a519963bc8f441eb73c71894edfb65");
				} else { // admin
					userService.addSuperUser("admin", System.getProperty("ADMIN_PASS", "admin"));
				}
			}
		};
	}

}
