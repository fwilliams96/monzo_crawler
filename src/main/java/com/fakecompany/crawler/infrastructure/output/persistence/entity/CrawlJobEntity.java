package com.fakecompany.crawler.infrastructure.output.persistence.entity;


import com.fakecompany.crawler.domain.CrawlJobStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "crawl_jobs")
@Data
public class CrawlJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String initialUrl;

    private Integer maxDepth;

    @Enumerated(EnumType.STRING)
    private CrawlJobStatus status;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String result;
}