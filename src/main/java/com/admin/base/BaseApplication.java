package com.admin.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseApplication.class, args);

    }
//    @Bean
//    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("spring.application.name") String applicationName) {
//        return (registry) ->  registry.config().commonTags("application", applicationName);
//    }

}
