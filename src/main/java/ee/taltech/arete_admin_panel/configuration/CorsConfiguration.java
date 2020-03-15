package ee.taltech.arete_admin_panel.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "*", // allow all
                                "http://localhost:8080", // dev front
                                "http://localhost:8002", // dev back
                                "http://localhost:8001", // back
                                "https://cs.ttu.ee/", // front
                                "http://localhost:8098") // tester
                        .allowedMethods("PUT", "DELETE", "GET", "POST")
                        .allowCredentials(true);
            }

        };
    }
}
