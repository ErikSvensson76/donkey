package com.example.donkey.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "document")
public record DocumentStorageProperty(
        String uploadDirectory
) {}
