package com.fakecompany.crawler.infrastructure.output.persistence.repository;

import com.fakecompany.crawler.infrastructure.output.persistence.entity.CrawlJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataJpaCrawlsRepository extends JpaRepository<CrawlJobEntity, UUID> {
}
