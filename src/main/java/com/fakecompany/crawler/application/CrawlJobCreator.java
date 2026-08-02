package com.fakecompany.crawler.application;

import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlJobStatus;
import com.fakecompany.crawler.domain.CrawlsRepository;
import com.fakecompany.crawler.domain.NewCrawlJob;
import com.fakecompany.crawler.shared.BadRequestError;
import com.fakecompany.crawler.shared.HttpUtils;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlJobCreator {

    private final CrawlsRepository crawlsRepository;
    private final CrawlJobExecutor crawlJobExecutor;

    public CrawlJob create(NewCrawlJob newCrawlJob) {
        if (!HttpUtils.isValidUrl(newCrawlJob.initialUrl())) {
            log.error("Initial url not valid: {}", newCrawlJob.initialUrl());
            throw new BadRequestError(newCrawlJob.initialUrl());
        }
        URI initialUrl = URI.create(newCrawlJob.initialUrl());
        CrawlJob crawlJob = CrawlJob.builder()
                .initialUrl(initialUrl)
                .maxDepth(newCrawlJob.maxDepth())
                .status(CrawlJobStatus.PENDING)
                .build();
        CrawlJob crawlJobCreated = crawlsRepository.create(crawlJob);
        crawlJobExecutor.execute(crawlJobCreated);
        return crawlJobCreated;
    }

}
