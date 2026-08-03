package com.fakecompany.crawler.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Getter
@Setter
@EnableAsync
@Configuration
@ConfigurationProperties(prefix = "async.config")
@EnableConfigurationProperties({AsyncConfig.class})
@Slf4j
public class AsyncConfig {

    private ExecutorConfig jobExecutor;
    private ExecutorConfig pageExecutor;

    @Bean("jobExecutor")
    @Profile("!test")
    public Executor jobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(jobExecutor.getCorePoolSize());
        executor.setMaxPoolSize(jobExecutor.getMaxPoolSize());
        executor.setQueueCapacity(jobExecutor.getQueueCapacity());
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.setThreadNamePrefix(jobExecutor.getThreadName());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("pageExecutor")
    public Executor pageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(pageExecutor.getCorePoolSize());
        executor.setMaxPoolSize(pageExecutor.getMaxPoolSize());
        executor.setQueueCapacity(pageExecutor.getQueueCapacity());
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.setThreadNamePrefix(pageExecutor.getThreadName());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * A {@link TaskDecorator} which copies {@link RequestContextHolder#currentRequestAttributes()} into the decorated
     * {@link Runnable} along with the logs {@link MDC}.
     * See https://stackoverflow.com/a/50138897/467944
     */
   static class ContextCopyingDecorator implements TaskDecorator {

        @Override
        public Runnable decorate(Runnable runnable) {
            RequestAttributes requestAttributes = null;
            try {
                requestAttributes = RequestContextHolder.currentRequestAttributes();
            } catch (IllegalStateException ignored) {
            }
            RequestAttributes finalRequestAttributes = requestAttributes;
            Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                  if (finalRequestAttributes != null) {
                    RequestContextHolder.setRequestAttributes(finalRequestAttributes);
                  }
                  if (mdcContextMap != null) {
                    MDC.setContextMap(mdcContextMap);
                  }
                  runnable.run();
                } finally {
                  MDC.clear();
                  RequestContextHolder.resetRequestAttributes();
                }
            };
        }

        @SuppressWarnings("unchecked")
        public static <T> T cast(Object object) {
            return (T) object;
        }
    }

}

@Getter
@Setter
class ExecutorConfig {
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private String threadName;
}