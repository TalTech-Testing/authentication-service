package ee.taltech.arete_admin_panel.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestAppConfiguration implements WebMvcConfigurer {

	private final LoggingComponent customRequestInterceptor;

	public RequestAppConfiguration(LoggingComponent customRequestInterceptor) {
		this.customRequestInterceptor = customRequestInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(customRequestInterceptor)
				.addPathPatterns("/**");

	}
}
