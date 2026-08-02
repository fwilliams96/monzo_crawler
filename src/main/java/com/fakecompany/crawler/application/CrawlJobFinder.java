package com.fakecompany.crawler.application;

import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrawlJobFinder {

    private final CrawlsRepository crawlsRepository;

    public Optional<CrawlJob> findById(UUID jobId) {
        return crawlsRepository.findCrawlJobById(jobId);
    }

}
