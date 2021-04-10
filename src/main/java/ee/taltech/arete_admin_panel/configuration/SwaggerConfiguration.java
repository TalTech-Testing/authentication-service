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

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfiguration {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Bean
	public OpenAPI customOpenAPI() {
		LOG.info("Loaded Swagger");

		List<Server> servers = new ArrayList<>();
		servers.add(new Server().url("https://cs.ttu.ee/").description("TalTech"));
		servers.add(new Server().url("https://arete-back.herokuapp.com/").description("Dev"));
		servers.add(new Server().url("http://localhost:8002/").description("Local Dev"));
		servers.add(new Server().url("http://localhost:8001/").description("Local Container Dev"));

		return new OpenAPI()
				.info(new Info()
						.title("Arete API")
						.version("2.0")
						.description("This is an API reference for Aretes that talks with automated testing service. use /auth endpoint to get a token which can be used to authorise one's self. You can use username: `admin`, password: `admin` to log into dev environment to test endpoints. Alternatively when endpoint supports an alternative authentication method, then that can be used instead.")
						.license(new License().name("GitHub").url("https://github.com/envomp?tab=repositories&q=arete&type=&language="))
						.contact(new Contact().email("enrico.vompa@gmail.com").name("Enrico Vompa"))
				)
				.servers(servers);
	}

}
