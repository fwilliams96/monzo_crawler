package com.fakecompany.crawler.infrastructure.input.rest;

import com.fakecompany.crawler.api.CrawlsApi;
import com.fakecompany.crawler.api.dto.*;
import com.fakecompany.crawler.application.CrawlJobCreator;
import com.fakecompany.crawler.application.CrawlJobFinder;
import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlResultPage;
import com.fakecompany.crawler.domain.NewCrawlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CrawlsController implements CrawlsApi {

    private final CrawlJobCreator crawlJobCreator;
    private final CrawlJobFinder crawlJobFinder;

    @Override
    public ResponseEntity<CrawlJobIdDto> createCrawlingJob(NewCrawlJobDto newCrawlJobDto) {
        NewCrawlJob newCrawlJob = new NewCrawlJob(newCrawlJobDto.getInitialUrl(), newCrawlJobDto.getMaxDepth());
        CrawlJob crawlJob = crawlJobCreator.create(newCrawlJob);
        CrawlJobIdDto crawlJobIdDto = new CrawlJobIdDto();
        crawlJobIdDto.setId(crawlJob.getId());
        return ResponseEntity.ok(crawlJobIdDto);
    }

    @Override
    public ResponseEntity<CrawlJobDto> getCrawlJob(UUID jobId) {
        Optional<CrawlJob> byId = crawlJobFinder.findById(jobId);
        return byId.map(crawlJob -> ResponseEntity.ok(mapToDto(crawlJob)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private CrawlJobDto mapToDto(CrawlJob crawlJob) {
        CrawlJobDto dto = new CrawlJobDto();
        dto.setId(crawlJob.getId());
        dto.setStatus(CrawlJobDto.StatusEnum.fromValue(crawlJob.getStatus().name()));
        dto.setInitialUrl(crawlJob.getInitialUrl().toString());
        dto.setMaxDepth(crawlJob.getMaxDepth());
        dto.setResult(mapToResult(crawlJob.getPages()));
        return dto;
    }

    private List<CrawlResultPageDto> mapToResult(List<CrawlResultPage> pages) {
        if (CollectionUtils.isEmpty(pages)) {
            return null;
        }
        return pages.stream().map(this::mapToResult).toList();
    }

    private CrawlResultPageDto mapToResult(CrawlResultPage page) {
        CrawlResultPageDto dto = new CrawlResultPageDto();
        dto.setUrl(page.url().toString());
        dto.setDepth(page.depth());
        dto.setLinks(mapLinksToList(page.links()));
        dto.setIncomingLinks(mapLinksToList(page.incomingLinks()));
        return dto;
    }

    private List<String> mapLinksToList(Set<URI> links) {
        return links.stream().map(URI::toString)
                .toList();
    }
}
