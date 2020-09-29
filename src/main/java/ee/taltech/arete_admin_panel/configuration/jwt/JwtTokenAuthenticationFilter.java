package ee.taltech.arete_admin_panel.configuration.jwt;

import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.AuthenticationDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseIdToken;
import ee.taltech.arete_admin_panel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;

public class JwtTokenAuthenticationFilter extends GenericFilterBean {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;

	public JwtTokenAuthenticationFilter(UserService userService, JwtTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
			throws IOException, ServletException, InvalidJwtAuthenticationException {

		try {
			HttpServletRequest request = (HttpServletRequest) req;
			String token = jwtTokenProvider.resolveToken(request);

			if (token != null) {
				LOG.info(MessageFormat.format("Trying to authenticate Authentication: {0}", token));

				if (jwtTokenProvider.validateToken(token)) {
					Authentication auth = userService.getAuthentication(token);

					if (auth != null) {
						LOG.info(MessageFormat.format("Authenticated user: {0} with authorities: {1}", userService.getUsername(token), auth.getAuthorities()));
						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				}
			} else {
				filterTestingTokens(request);
				filterGitlabHooks(request);
				filterDockerHooks(request);
			}
		} catch (Exception e) {
			LOG.error("JWT authentication failed with message: {}", e.getMessage());
		} finally {
			filterChain.doFilter(req, res);
		}
	}

	private void filterTestingTokens(HttpServletRequest request) {

		String token = request.getHeader("X-Testing-Token");
		filterHooks(token, "X-Testing-Token");

	}

	private void filterDockerHooks(HttpServletRequest request) {

		String token = request.getHeader("X-Docker-Token");
		filterHooks(token, "X-Docker-Token");

	}

	private void filterGitlabHooks(HttpServletRequest request) {

		String token = request.getHeader("X-Gitlab-Token");
		filterHooks(token, "X-Gitlab-Token");

	}

	private void filterHooks(String token, String name) {
		if (token != null) {
			LOG.info(MessageFormat.format("Trying to authenticate {0}: {1}", name, token));
			String[] parts = token.split(" ");
			String gitlabToken = parts[1];
			String username = parts[0];
			UserResponseIdToken user = userService.authenticateUser(new AuthenticationDto(username, gitlabToken));

			User userDetails = userService.getUser(user.getId());
			Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

	}
}