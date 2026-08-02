package com.fakecompany.crawler.infrastructure.config;

import com.fakecompany.crawler.infrastructure.output.rest.RestClientPageHtmlGetter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BeansConfig {

    @Bean
    public RestClientPageHtmlGetter pageHttpClient(RestClient.Builder builder) {
        return new RestClientPageHtmlGetter(builder);
    }

}
