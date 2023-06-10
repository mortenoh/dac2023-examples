package com.example.dhis2;

import com.example.dhis2.configuration.MainProperties;
import lombok.RequiredArgsConstructor;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.hisp.dhis.integration.sdk.Dhis2ClientBuilder;
import org.hisp.dhis.integration.sdk.api.Dhis2Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.Servlet;

@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties({MainProperties.class})
public class Application {

    private final MainProperties properties;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean<Servlet> servletRegistrationBean()
    {
        ServletRegistrationBean<Servlet> registration = new ServletRegistrationBean<>( new CamelHttpTransportServlet(),
                "/dhis/*" );
        registration.setName( "CamelServlet" );
        return registration;
    }

    @Bean
    public Dhis2Client dhis2Client() {
        return Dhis2ClientBuilder
                .newClient(
                        properties.getSource().getBaseUrl(),
                        properties.getSource().getUsername(),
                        properties.getSource().getPassword())
                .build();
    }

    @Bean
    public Dhis2Client dhis2ClientTarget() {
        return Dhis2ClientBuilder
                .newClient(
                        properties.getTarget().getBaseUrl(),
                        properties.getTarget().getUsername(),
                        properties.getTarget().getPassword())
                .build();
    }
}
