package com.fakecompany.crawler.application;

import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlJobStatus;
import com.fakecompany.crawler.domain.CrawlsRepository;
import com.fakecompany.crawler.infrastructure.TestAsyncConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAsyncConfiguration.class)
@Sql(
        statements = "TRUNCATE TABLE crawl_jobs RESTART IDENTITY CASCADE;",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CrawlJobExecutorITTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("integration_tests")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private CrawlJobExecutor crawlJobExecutor;

    @Autowired
    private CrawlsRepository crawlsRepository;

    @Test
    void shouldCrawlWebSite() {
        URI initialUrl = URI.create("https://crawlme.monzo.com/");
        Integer maxDepth = 5;

        CrawlJob crawlJob = CrawlJob.builder()
                .initialUrl(initialUrl)
                .maxDepth(maxDepth)
                .status(CrawlJobStatus.PENDING)
                .build();

        CrawlJob crawlJobCreated = crawlsRepository.create(crawlJob);

        crawlJobExecutor.execute(crawlJobCreated);

        CrawlJob result = crawlsRepository
                .findCrawlJobById(crawlJobCreated.getId())
                .orElseThrow();

        assertEquals(CrawlJobStatus.FINISHED, result.getStatus());
        assertNotNull(result.getPages());
        assertFalse(result.getPages().isEmpty());
    }

}