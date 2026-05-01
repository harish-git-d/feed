package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.domain.FeedDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FeedDefinitionLoader {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public FeedDefinition load(String feedName) {
        String resourcePath = "feed-definitions/" + feedName + ".yml";
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalArgumentException("Feed definition not found: " + resourcePath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return mapper.readValue(inputStream, FeedDefinition.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read feed definition " + resourcePath, e);
        }
    }
}
