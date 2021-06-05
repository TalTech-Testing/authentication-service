package ee.taltech.arete_admin_panel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AreteAdminPanelApplication {

	public static void main(String[] args) {
		SpringApplication.run(AreteAdminPanelApplication.class, args);
	}

}
