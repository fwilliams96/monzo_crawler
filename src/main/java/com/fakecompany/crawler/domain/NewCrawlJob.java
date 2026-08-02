package com.fakecompany.crawler.domain;

public record NewCrawlJob (
        String initialUrl,
        Integer maxDepth
) { }
