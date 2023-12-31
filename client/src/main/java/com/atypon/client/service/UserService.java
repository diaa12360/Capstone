package com.atypon.client.service;

import com.atypon.client.model.AuthRequest;
import com.atypon.client.model.Collection;
import com.atypon.client.model.Document;
import com.atypon.client.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {
    private final String bootstrapUrl;
    private String nodeUrl;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;

    @Autowired
    public UserService(RestTemplate restTemplate, @Value("${bootstrap.url}") String bootstrapUrl, HttpHeaders headers) {
        this.bootstrapUrl = bootstrapUrl;
        this.restTemplate = restTemplate;
        this.headers = headers;
    }

    public String login(AuthRequest authRequest) {
        if (Objects.equals(nodeUrl, "") || nodeUrl == null) nodeUrl(authRequest.getUsername());
        authRequest.setPassword(Encryption.encrypt(authRequest.getPassword()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String token = restTemplate.postForEntity(nodeUrl.concat("/auth/login"), authRequest, String.class, headers).getBody();
        if (token != null) {
            headers.setBearerAuth(token);
        } else
            throw new RuntimeException();
        return token;
    }


    public String nodeUrl(String username) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(bootstrapUrl.concat("/user/get-node-url?username=").concat(username), String.class);
        nodeUrl = responseEntity.getBody();
        return nodeUrl;
    }

    public String connectToDatabase(String dbName) {
        HttpEntity<String> entityReq = new HttpEntity<>(dbName, headers);
        return restTemplate.exchange(
                nodeUrl.concat("/user/connect-to-database?dbName=").concat(dbName),
                HttpMethod.GET, entityReq, String.class).getBody();
    }

    public void createDatabase(String dbName) {
        HttpEntity<String> entityReq = new HttpEntity<>(dbName, headers);
        restTemplate.exchange(
                nodeUrl.concat("/user/create-database?dbName=").concat(dbName),
                HttpMethod.POST, entityReq, String.class);
    }

    public void deleteDatabase(String dbName) {
        restTemplate.exchange(nodeUrl.concat("/user/delete-database?dbName=").concat(dbName),
                HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);
    }

    public Collection createCollection(Collection collection) {
        return restTemplate.exchange(
                nodeUrl.concat("/user/create-collection"),
                HttpMethod.POST,
                new HttpEntity<>(collection, headers),
                Collection.class).getBody();
    }

    public void deleteCollection(Collection collection) {
        restTemplate.exchange(
                nodeUrl.concat("/user/delete-collection"),
                HttpMethod.DELETE,
                new HttpEntity<>(collection, headers),
                Object.class);
    }

    public Document createRecord(Document document) {
        return restTemplate.exchange(
                nodeUrl.concat("/user/create-document"),
                HttpMethod.POST,
                new HttpEntity<>(document, headers),
                Document.class
        ).getBody();
    }

    public void deleteRecord(Document document) {
        restTemplate.exchange(
                nodeUrl.concat("/user/delete-document"),
                HttpMethod.DELETE,
                new HttpEntity<>(document, headers),
                Document.class
        );
    }

    public Document modifyRecord(Document document) {
        return restTemplate.exchange(
                nodeUrl.concat("/user/modify-document"),
                HttpMethod.PUT,
                new HttpEntity<>(document, headers),
                Document.class
        ).getBody();
    }

    public Document getDataOne(String collectionName, String prop, Object value) {
        return Objects.requireNonNull(
                restTemplate.exchange(
                        nodeUrl
                                .concat("/user/find?collection=").concat(collectionName).concat("&")
                                .concat("property=").concat(prop).concat("&").concat("value=").concat(String.valueOf(value)),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Document.class
                ).getBody()
        );
    }

    public List<Document> getAll(String collectionName, String prop, String value) {
        return Objects.requireNonNull(
                restTemplate.exchange(
                        nodeUrl
                                .concat("/user/find-all?collection=").concat(collectionName).concat("&")
                                .concat("property=").concat(prop).concat("&").concat("value=").concat(String.valueOf(value)),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        List.class
                ).getBody()
        );
    }

    public List<String> getDatabases() {
        return Objects.requireNonNull(
                restTemplate.exchange(
                        nodeUrl.concat("/user/get-databases"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        List.class
                ).getBody()
        );
    }

    public List<String> getCollections() {
        return Objects.requireNonNull(
                restTemplate.exchange(
                        nodeUrl.concat("/user/get-collections"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        List.class
                ).getBody()
        );
    }

    public String getProps(String collectionName) {
        return Objects.requireNonNull(
                restTemplate.exchange(
                        nodeUrl.concat("/user/get-props?collectionName=").concat(collectionName),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                ).getBody()
        );
    }
}
