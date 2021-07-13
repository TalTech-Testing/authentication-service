package ee.taltech.arete_admin_panel.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.java.LoadBalancerClient;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ApplicationProperties {

    @Bean
    @Scope("prototype")
    public Logger produceLogger(InjectionPoint injectionPoint) {
        Class<?> classOnWired = injectionPoint.getMember().getDeclaringClass();
        return LoggerFactory.getLogger(classOnWired);
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    public LoadBalancerClient client(Logger logger) {
        String url = System.getenv("TESTER_URL");
        if (url == null) {
            logger.warn("TESTER_URL env parameter is not set. Defaulting to http://127.0.0.1:8098");
            url = "http://127.0.0.1:8098";
        }
        return new LoadBalancerClient(url);
    }
}

