package com.fakecompany.crawler.infrastructure.output.rest;

import com.fakecompany.crawler.domain.PageHtmlGetter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.net.URI;

@RequiredArgsConstructor
public class RestClientPageHtmlGetter implements PageHtmlGetter {

    private final RestClient restClient;

    public RestClientPageHtmlGetter(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String getHtml(URI uri) {
        return restClient.get()
                .uri(uri)
                .header("User-Agent", "TakeHomeCrawler/1.0")
                .accept(MediaType.TEXT_HTML)
                .retrieve()
                .body(String.class);
    }
}
