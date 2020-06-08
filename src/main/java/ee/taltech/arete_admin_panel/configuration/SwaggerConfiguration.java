package ee.taltech.arete_admin_panel.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Bean
	public OpenAPI customOpenAPI() {
		LOG.info("Loaded Swagger");
		return new OpenAPI()
				.info(new Info()
						.title("Arete API")
						.version("2.0")
						.description("This is an API reference for Aretes that talks with automated testing service. use /auth endpoint to get a token which can be used to authorise one's self")
						.license(new License().name("GitHub").url("https://github.com/envomp?tab=repositories&q=arete&type=&language="))
						.contact(new Contact().email("enrico.vompa@gmail.com").name("Enrico Vompa"))
				)
				.servers(List.of(new Server().url("https://cs.ttu.ee/").description("TalTech")));
	}


}
