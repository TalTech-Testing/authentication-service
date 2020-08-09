package ee.taltech.arete_admin_panel.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.time.Instant;

@Component
public class LoggingComponent extends HandlerInterceptorAdapter {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

		long startTime = Instant.now().toEpochMilli();
		request.setAttribute("startTime", startTime);
		request.setAttribute("originalURL", request.getRequestURL().toString());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

		long startTime = (Long) request.getAttribute("startTime");
		String originalURL = (String) request.getAttribute("originalURL");
		LOG.info(MessageFormat.format("Request URL::{0} {1}:: Status Code={2}:: Time Taken={3} ms",
				request.getMethod(),
				originalURL, // otherwise /error would be displayed
				response.getStatus(),
				Instant.now().toEpochMilli() - startTime)
		);
	}
}
