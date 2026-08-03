package com.fakecompany.crawler.infrastructure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@TestConfiguration(proxyBeanMethods = false)
public class TestAsyncConfiguration {

    @Bean("jobExecutor")
    public Executor jobExecutor() {
        return new SyncTaskExecutor();
    }
}
