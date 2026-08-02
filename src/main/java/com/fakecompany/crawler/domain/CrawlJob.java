package com.fakecompany.crawler.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@ToString
public class CrawlJob {

    private UUID id;

    private URI initialUrl;

    private Integer maxDepth;

    private CrawlJobStatus status;

    private List<CrawlResultPage> pages;

    public void finishCrawl(List<CrawlResultPage> pages) {
        this.pages = pages;
        this.status = CrawlJobStatus.FINISHED;
    }

}
