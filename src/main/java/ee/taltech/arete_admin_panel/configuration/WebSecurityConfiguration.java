package ee.taltech.arete_admin_panel.configuration;

import ee.taltech.arete_admin_panel.configuration.jwt.JwtSecurityConfigurer;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
import ee.taltech.arete_admin_panel.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
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
@AllArgsConstructor
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	final Logger logger;
	final UserService userService;
	final JwtTokenProvider jwtTokenProvider;

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
				.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/error").permitAll() // error
				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/docs/**").permitAll() // swagger
				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/swagger-ui/**").permitAll() // swagger

				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/user/auth").permitAll() // login
				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/user").hasAuthority("ADMIN") // people cant register
				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/user/any").hasAuthority("ADMIN") // admin can register all roles
				.antMatchers(HttpMethod.PUT, "/services/arete/api/v2/user").hasAnyAuthority("ADMIN") // TODO: people can change their data
				.antMatchers(HttpMethod.DELETE, "/services/arete/api/v2/user").hasAnyAuthority("ADMIN") // TODO: people can delete their accounts and admin can delete every ones (except admin)
				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/user/all").hasAuthority("ADMIN") // admin can see users

				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/submission").hasAnyAuthority("ADMIN", "DEVELOPER", "TESTER") // tester callback.
				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/submission/**").hasAnyAuthority("ADMIN", "DEVELOPER") // TODO: people can see their stuff and admin can see all
				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/submission/**").hasAnyAuthority("ADMIN", "DEVELOPER", "HOOK", "TESTER") // TODO: people can run tests

				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/student/**").hasAnyAuthority("ADMIN", "DEVELOPER") // TODO: people can see their stuff and admin can see all

				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/state").hasAnyAuthority("ADMIN", "DEVELOPER") // TODO: people can see state
				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/state/**").hasAnyAuthority("ADMIN", "DEVELOPER") // TODO: people can see state

				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/exercise/**").hasAnyAuthority("ADMIN", "DEVELOPER") // TODO: people can see their stuff and admin can see all
				.antMatchers(HttpMethod.PUT, "/services/arete/api/v2/exercise").hasAnyAuthority("ADMIN", "DEVELOPER") // admin can update tests
				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/exercise").hasAnyAuthority("HOOK", "ADMIN", "DEVELOPER") // admin and hook can update tests

				.antMatchers(HttpMethod.GET, "/services/arete/api/v2/course/**").hasAnyAuthority("ADMIN") // TODO: people can see their stuff and admin can see all
				.antMatchers(HttpMethod.PUT, "/services/arete/api/v2/course/**").hasAnyAuthority("ADMIN") // admin docker images
				.antMatchers(HttpMethod.POST, "/services/arete/api/v2/course/**").hasAnyAuthority("ADMIN", "DEVELOPER", "HOOK") // admin and hook can update course docker images

				.anyRequest().authenticated()
				.and()
				.apply(new JwtSecurityConfigurer(logger, userService, jwtTokenProvider));

	}
}
