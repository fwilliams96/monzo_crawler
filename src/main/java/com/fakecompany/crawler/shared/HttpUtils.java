package com.fakecompany.crawler.shared;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class HttpUtils {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private HttpUtils() {}

    public static boolean isValidUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            return isHttpUrl(uri);
        }
        catch (Exception e) {
            log.error("Error casting url {} to URI", url);
            return false;
        }
    }

    public static boolean isHttpUrl(URI uri) {
        return HTTP.equalsIgnoreCase(uri.getScheme())
                || HTTPS.equalsIgnoreCase(uri.getScheme());
    }

}
