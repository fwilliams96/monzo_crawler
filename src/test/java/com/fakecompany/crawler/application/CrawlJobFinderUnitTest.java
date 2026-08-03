package com.fakecompany.crawler.application;

import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlJobStatus;
import com.fakecompany.crawler.domain.CrawlsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CrawlJobFinderUnitTest {

    @InjectMocks
    private CrawlJobFinder crawlJobFinder;

    @Mock
    private CrawlsRepository crawlsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_find() {
        // Given
        UUID jobId = UUID.randomUUID();
        CrawlJob crawlJob = CrawlJob.builder()
                .id(jobId)
                .initialUrl(URI.create("https://crawlme.monzo.com/"))
                .status(CrawlJobStatus.PENDING)
                .maxDepth(50)
                .build();

        when(crawlsRepository.findCrawlJobById(eq(jobId))).thenReturn(Optional.of(crawlJob));

        // When
        Optional<CrawlJob> byId = crawlJobFinder.findById(jobId);

        // Then
        assertTrue(byId.isPresent());
        assertEquals(jobId, byId.get().getId());
        assertEquals(CrawlJobStatus.PENDING, byId.get().getStatus());
    }

    @Test
    void should_not_find() {
        // Given
        when(crawlsRepository.findCrawlJobById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        UUID nonExistingJobId = UUID.randomUUID();
        Optional<CrawlJob> byId = crawlJobFinder.findById(nonExistingJobId);

        // Then
        assertFalse(byId.isPresent());
    }

}