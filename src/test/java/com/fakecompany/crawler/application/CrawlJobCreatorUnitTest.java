package com.fakecompany.crawler.application;

import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlJobStatus;
import com.fakecompany.crawler.domain.CrawlsRepository;
import com.fakecompany.crawler.domain.NewCrawlJob;
import com.fakecompany.crawler.shared.BadRequestError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CrawlJobCreatorUnitTest {

    @InjectMocks
    private CrawlJobCreator crawlJobCreator;

    @Mock
    private CrawlsRepository crawlsRepository;

    @Mock
    private CrawlJobExecutor crawlJobExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_create() {
        // Given
        NewCrawlJob newCrawlJob = new NewCrawlJob("https://crawlme.monzo.com/", 50);
        when(crawlsRepository.create(any(CrawlJob.class))).thenAnswer(invocationOnMock -> {
            CrawlJob crawlJob = (CrawlJob) invocationOnMock.getArguments()[0];
            return CrawlJob.builder()
                    .id(UUID.randomUUID())
                    .status(crawlJob.getStatus())
                    .maxDepth(crawlJob.getMaxDepth())
                    .initialUrl(crawlJob.getInitialUrl())
                    .pages(crawlJob.getPages())
                    .build();
        });

        // When
        CrawlJob crawlJobCreated = crawlJobCreator.create(newCrawlJob);

        // Then
        assertNotNull(crawlJobCreated.getId());
        assertEquals(CrawlJobStatus.PENDING, crawlJobCreated.getStatus());
    }

    @ParameterizedTest()
    @MethodSource("invalidCrawlUrls")
    void should_not_create(String initialUrl) {
        // Given
        NewCrawlJob newCrawlJob = new NewCrawlJob(initialUrl, 50);
        when(crawlsRepository.create(any(CrawlJob.class))).thenAnswer(invocationOnMock -> {
            CrawlJob crawlJob = (CrawlJob) invocationOnMock.getArguments()[0];
            return CrawlJob.builder()
                    .id(UUID.randomUUID())
                    .status(crawlJob.getStatus())
                    .maxDepth(crawlJob.getMaxDepth())
                    .initialUrl(crawlJob.getInitialUrl())
                    .pages(crawlJob.getPages())
                    .build();
        });

        // When
        BadRequestError badRequestError = assertThrows(BadRequestError.class, () -> crawlJobCreator.create(newCrawlJob));

        // Then
        assertNotNull(badRequestError);
        assertTrue(badRequestError.getMessage().contains(initialUrl == null ? "null" : initialUrl));
    }

    private static Stream<String> invalidCrawlUrls() {
        return Stream.of(
                null,
                "",
                "crawlme.monzo.com/",
                "crawlme"
        );
    }

}