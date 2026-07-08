package com.admin.base;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.admin.base.mapper")
public class BaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseApplication.class, args);

    }
//    @Bean
//    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("spring.application.name") String applicationName) {
//        return (registry) ->  registry.config().commonTags("application", applicationName);
//    }

}
