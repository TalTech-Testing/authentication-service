package ee.taltech.arete_admin_panel.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@Configuration
@EnableScheduling
public class PropertyConfiguration {
}
