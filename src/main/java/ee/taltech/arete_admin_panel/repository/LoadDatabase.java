package ee.taltech.arete_admin_panel.repository;

import ee.taltech.arete_admin_panel.pojo.abi.users.UserPostDto;
import ee.taltech.arete_admin_panel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
