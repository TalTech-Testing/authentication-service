package ee.taltech.arete_admin_panel.configuration;

import ee.taltech.arete_admin_panel.configuration.jwt.JwtSecurityConfigurer;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
import ee.taltech.arete_admin_panel.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	final UserService userService;
	final JwtTokenProvider jwtTokenProvider;

	public WebSecurityConfiguration(UserService userService, JwtTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
				.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.cors()
				.and()
				.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/error").permitAll() // error
				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/docs/**").permitAll() // swagger
				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/swagger-ui/**").permitAll() // swagger
				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/user/auth").permitAll() // login
				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/submission").permitAll() // tester callback. Protected by shared secret
				.anyRequest().authenticated()
				.and()
				.apply(new JwtSecurityConfigurer(userService, jwtTokenProvider))
		;

	}
}
