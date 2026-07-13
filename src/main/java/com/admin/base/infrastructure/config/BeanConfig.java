package com.admin.base.infrastructure.config;

import com.admin.base.shared.util.JwtTokenUtil;
import com.admin.base.shared.util.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/8/24 9:56 下午
 * @desc
 */
@Configuration
public class BeanConfig {
    /**
     * 线程池
     * @return
     */
    @Bean("threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.initialize();
        threadPoolTaskExecutor.setCorePoolSize(20);
        threadPoolTaskExecutor.setCorePoolSize(20);
        threadPoolTaskExecutor.setThreadNamePrefix("并发任务池子---");
        return threadPoolTaskExecutor;
    }


    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(50,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }

    /**
     * restTemplate  请求配置
     * @return
     */
    @Bean("restTemplate")
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(6000);
        simpleClientHttpRequestFactory.setConnectTimeout(6000);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        return restTemplate;
    }



    /**
     * bean的参数校验执行器
     *
     * @return
     */
    @Bean
    public BeanPostProcessor beanValidationPostProcessor() {
        return new BeanValidationPostProcessor();
    }

    /**
     * Jwt token
     * @return
     */
    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }
}
