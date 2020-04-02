package ee.taltech.arete_admin_panel.configuration;

import ee.taltech.arete_admin_panel.configuration.jwt.JwtSecurityConfigurer;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    JwtTokenProvider jwtTokenProvider;

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
                    .antMatchers(HttpMethod.GET, "/services/arete/api/v1/**").permitAll() // docs
                    .antMatchers(HttpMethod.POST, "/services/arete/api/v1/**").permitAll() // docs
                    .antMatchers(HttpMethod.POST, "/services/arete/api/admin/auth").permitAll() // login
                    .antMatchers(HttpMethod.POST, "/services/arete/api/admin/job").permitAll() // tester feedback. Protected by shared secret
                .anyRequest().permitAll()
                .and()
                .apply(new JwtSecurityConfigurer(jwtTokenProvider));

    }
}
