package com.fakecompany.crawler.application;

import com.fakecompany.crawler.domain.CrawlJob;
import com.fakecompany.crawler.domain.CrawlResultPage;
import com.fakecompany.crawler.domain.CrawlsRepository;
import com.fakecompany.crawler.domain.PageHtmlGetter;
import com.fakecompany.crawler.shared.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlJobExecutor {

    private final CrawlsRepository repository;
    private final PageHtmlGetter pageHtmlGetter;
    private final @Qualifier("pageExecutor") Executor pageExecutor;

    public record CrawlTaskNode(URI url, int depth, Set<URI> incomingLinks, Set<URI> links) {}
    public record PageResult(List<URI> links) {}
    private record CrawlTaskResult(CrawlTaskNode task, PageResult result) {}

    @Async("jobExecutor")
    public void execute(CrawlJob crawlJob) {
        log.info("Executing crawl for link {} and with max depth {}", crawlJob.getInitialUrl(), crawlJob.getMaxDepth());

        Map<URI, CrawlTaskNode> visited = new ConcurrentHashMap<>();

        CrawlTaskNode rootNode = new CrawlTaskNode(crawlJob.getInitialUrl(), 0, ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
        visited.put(crawlJob.getInitialUrl(), rootNode);

        List<CrawlTaskNode> currentLevel = List.of(rootNode);

        while (!currentLevel.isEmpty()) {

            List<CompletableFuture<CrawlTaskResult>> futures = currentLevel.stream()
                    .map(node -> CompletableFuture.supplyAsync(
                            () -> {
                                PageResult result = crawl(node.url());
                                return new CrawlTaskResult(node, result);
                            },
                            pageExecutor
                    ))
                    .toList();

            CompletableFuture.allOf(
                    futures.toArray(CompletableFuture[]::new)
            ).join();

            List<CrawlTaskResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            log.info("Number of task results found for current level with size {} : {}", currentLevel.size(), results.size());

            List<CrawlTaskNode> nextLevel = new ArrayList<>();

            for (CrawlTaskResult crawlTaskResult : results) {
                CrawlTaskNode current = crawlTaskResult.task();

                current.links().addAll(crawlTaskResult.result().links());

                if (crawlJob.getMaxDepth() != null && current.depth() >= crawlJob.getMaxDepth()) {
                    log.info("Reached max depth {}, ending crawling...", crawlJob.getMaxDepth());
                    continue;
                }

                log.info("Number of links found for node(link={}, depth={}) : {}", crawlTaskResult.task().url(), crawlTaskResult.task().depth(), crawlTaskResult.result().links());

                for (URI link: crawlTaskResult.result().links()) {
                    if (!link.toString().startsWith(crawlJob.getInitialUrl().toString())) {
                        // We only want urls that contains the same subdomain, we can do it comparing with the initial url
                        continue;
                    }

                    CrawlTaskNode newNode = new CrawlTaskNode(
                            link,
                            current.depth() + 1,
                            ConcurrentHashMap.newKeySet(),
                            ConcurrentHashMap.newKeySet());

                    CrawlTaskNode existing =
                            visited.putIfAbsent(link, newNode);

                    CrawlTaskNode target =
                            existing != null ? existing : newNode;

                    target.incomingLinks().add(current.url());

                    if (existing == null) {
                        log.debug("Link {} is not yet visited, adding new node with depth {}...", link, current.depth() + 1);
                        nextLevel.add(newNode);
                    }
                    else {
                        log.debug("Link {} is already visited in node with depth {}, adding url {} to incomingLinks...", link, existing.depth(), current.url());
                    }
                }

            }
            currentLevel = nextLevel;
        }
        List<CrawlResultPage> pages = mapCrawlTaskNodesToResultPages(visited);
        log.info("Crawling finished, pages found: {}", pages);
        crawlJob.finishCrawl(pages);
        repository.update(crawlJob);
    }

    private List<CrawlResultPage> mapCrawlTaskNodesToResultPages(Map<URI, CrawlTaskNode> visited) {
        if (visited.isEmpty()) {
            return Collections.emptyList();
        }
        return visited.values().stream()
                .map(crawlTaskNode ->
                        new CrawlResultPage(
                                crawlTaskNode.url(),
                                crawlTaskNode.depth(),
                                crawlTaskNode.links(),
                                crawlTaskNode.incomingLinks()
                        )
                ).toList();
    }

    private PageResult crawl(URI uri) {
        String html = pageHtmlGetter.getHtml(uri);
        Document document = Jsoup.parse(html, uri.toString());

        List<URI> links = document
                .select("a[href]")
                .stream()
                .map(element -> element.absUrl("href"))
                .filter(href -> !href.isBlank())
                .map(this::toUri)
                .filter(Objects::nonNull)
                .filter(this::isHttpUrl)
                .distinct()
                .toList();

        log.info("{} links found for uri {}", links.size(), uri);
        log.info("Links found for uri {}: {}", uri, links);

        return new PageResult(links);
    }

    private URI toUri(String value) {
        try {
            return URI.create(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isHttpUrl(URI uri) {
        return HttpUtils.isHttpUrl(uri);
    }

}
