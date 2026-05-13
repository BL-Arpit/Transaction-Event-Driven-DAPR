package com.bl.poc.aggregator.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class DaprStateStoreClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${dapr.state.url}")
    private String daprStateUrl;

    public DaprStateStoreClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void saveState(String key, Object value) {

        List<Map<String, Object>> requestBody =
                List.of(
                        Map.of(
                                "key", key,
                                "value", value
                        )
                );

        restTemplate.postForEntity(
                daprStateUrl,
                requestBody,
                Void.class
        );
    }

    public <T> T getState(
            String key,
            Class<T> responseType,
            T defaultValue
    ) {
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(
                            daprStateUrl + "/" + key,
                            JsonNode.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null
                    || response.getBody().isNull()
                    || response.getBody().isMissingNode()) {
                return defaultValue;
            }

            return objectMapper.treeToValue(
                    response.getBody(),
                    responseType
            );

        } catch (Exception exception) {
            return defaultValue;
        }
    }

    public <T> List<T> getListState(
            String key,
            Class<T> elementType
    ) {
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(
                            daprStateUrl + "/" + key,
                            JsonNode.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null
                    || response.getBody().isNull()
                    || response.getBody().isMissingNode()) {
                return Collections.emptyList();
            }

            JavaType listType =
                    objectMapper
                            .getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    elementType
                            );

            return objectMapper.convertValue(
                    response.getBody(),
                    listType
            );

        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    public void deleteState(String key) {
        restTemplate.delete(daprStateUrl + "/" + key);
    }
}