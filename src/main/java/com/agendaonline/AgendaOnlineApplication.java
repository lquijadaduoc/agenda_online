package com.agendaonline;

import com.agendaonline.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AgendaOnlineApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendaOnlineApplication.class, args);
    }
}
