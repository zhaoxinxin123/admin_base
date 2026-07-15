package com.admin.base.support;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.datasource.url=${DEV_DATASOURCE_URL:jdbc:mysql://192.168.3.3:3306/admin_base_it?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false&connectTimeout=3000&socketTimeout=3000}",
        "spring.datasource.username=${DEV_DATASOURCE_USERNAME:root}",
        "spring.datasource.password=${DEV_DATASOURCE_PASSWORD:hjdz@10086}",
        "spring.datasource.druid.initial-size=1",
        "spring.datasource.druid.min-idle=0",
        "spring.datasource.druid.max-active=2",
        "spring.datasource.druid.max-wait=3000",
        "spring.datasource.druid.connection-error-retry-attempts=0",
        "spring.datasource.druid.break-after-acquire-failure=true",
        "spring.datasource.druid.time-between-connect-error-millis=1000",
        "spring.data.redis.host=${DEV_REDIS_HOST:192.168.3.3}",
        "spring.data.redis.port=${DEV_REDIS_PORT:6379}",
        "spring.data.redis.password=${DEV_REDIS_PASSWORD:hjdz@10086}",
        "spring.data.redis.timeout=3s",
        "spring.data.redis.connect-timeout=3s",
        "sysconfig.upload-path=/private/tmp/admin-base-dev-test/upload",
        "sysconfig.download-path=/private/tmp/admin-base-dev-test/download",
        "sysconfig.local-store=/private/tmp/admin-base-dev-test/local"
})
public @interface DevRemoteIntegrationTest {
}
