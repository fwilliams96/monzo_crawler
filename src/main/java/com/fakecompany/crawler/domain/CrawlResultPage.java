package com.fakecompany.crawler.domain;

import java.net.URI;
import java.util.Set;

public record CrawlResultPage (
        URI url,
        Integer depth,
        Set<URI> links,
        Set<URI> incomingLinks
) { }
