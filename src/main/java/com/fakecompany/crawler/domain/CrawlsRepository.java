package com.fakecompany.crawler.domain;

import java.util.Optional;
import java.util.UUID;

public interface CrawlsRepository {

    CrawlJob create(CrawlJob crawlJob);

    Optional<CrawlJob> findCrawlJobById(UUID jobId);

    CrawlJob update(CrawlJob crawlJob);

}
