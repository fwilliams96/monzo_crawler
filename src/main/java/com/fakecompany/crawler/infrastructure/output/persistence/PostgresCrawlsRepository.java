package com.fakecompany.crawler.infrastructure.output.persistence;

import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlResultPage;
import com.fakecompany.crawler.domain.CrawlsRepository;
import com.fakecompany.crawler.infrastructure.output.persistence.entity.CrawlJobEntity;
import com.fakecompany.crawler.infrastructure.output.persistence.repository.SpringDataJpaCrawlsRepository;
import com.fakecompany.crawler.shared.InternalServerError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostgresCrawlsRepository implements CrawlsRepository {

    private final SpringDataJpaCrawlsRepository springDataJpaCrawlsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public CrawlJob create(CrawlJob crawlJob) {
        CrawlJobEntity entity = springDataJpaCrawlsRepository.save(mapToEntity(crawlJob));
        return mapToDomain(entity);
    }

    @Override
    public Optional<CrawlJob> findCrawlJobById(UUID jobId) {
        return springDataJpaCrawlsRepository.findById(jobId).map(this::mapToDomain);
    }

    @Override
    public CrawlJob update(CrawlJob crawlJob) {
        CrawlJobEntity entity = springDataJpaCrawlsRepository.save(mapToEntity(crawlJob));
        return mapToDomain(entity);
    }

    private CrawlJobEntity mapToEntity(CrawlJob crawlJob) {
        CrawlJobEntity entity = new CrawlJobEntity();
        entity.setId(crawlJob.getId());
        entity.setInitialUrl(crawlJob.getInitialUrl().toString());
        entity.setMaxDepth(crawlJob.getMaxDepth());
        entity.setStatus(crawlJob.getStatus());
        if (!CollectionUtils.isEmpty(crawlJob.getPages())) {
            try {
                entity.setResult(objectMapper.writeValueAsString(crawlJob.getPages()));
            } catch (JsonProcessingException e) {
                throw new InternalServerError("Error converting result pages to string", e);
            }
        }
        return entity;
    }

    private CrawlJob mapToDomain(CrawlJobEntity entity) {
        URI uri = URI.create(entity.getInitialUrl());
        List<CrawlResultPage> crawlResultPages = null;
        if (StringUtils.isNotBlank(entity.getResult())) {
            try {
                crawlResultPages = objectMapper.readValue(entity.getResult(), new TypeReference<>() {});
                crawlResultPages.sort(Comparator.comparingInt(CrawlResultPage::depth));
            } catch (JsonProcessingException e) {
                throw new InternalServerError("Error converting string to result pages", e);
            }
        }
        return CrawlJob.builder()
                .id(entity.getId())
                .initialUrl(uri)
                .maxDepth(entity.getMaxDepth())
                .status(entity.getStatus())
                .pages(crawlResultPages)
                .build();
    }
}
