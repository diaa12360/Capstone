package com.atypon.client.service;

import com.atypon.client.model.AuthRequest;
import com.atypon.client.model.Collection;
import com.atypon.client.model.Document;
import com.atypon.client.model.User;
import jdk.jfr.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class UserService {
    private final String bootstrapUrl;
    private String nodeUrl;
    private final RestTemplate restTemplate;
    private String token;
    private final HttpHeaders headers;

    private String dbName;

    @Autowired
    public UserService(RestTemplate restTemplate, @Value("${bootstrap.url}") String bootstrapUrl, HttpHeaders headers) {
        this.bootstrapUrl = bootstrapUrl;
        this.restTemplate = restTemplate;
        this.headers = headers;
    }

    public User createAccount(User user) {
        ResponseEntity<User> responseEntity = restTemplate.postForEntity(bootstrapUrl.concat("/user/create-account"), user, User.class);
        return responseEntity.getBody();
    }

    public String login(AuthRequest authRequest) {
        if (Objects.equals(nodeUrl, "") || nodeUrl == null) nodeUrl(authRequest.getUsername());
        authRequest.setPassword(Encryption.encrypt(authRequest.getPassword()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        token = restTemplate.postForEntity(nodeUrl.concat("/auth/login"), authRequest, String.class, headers).getBody();
        assert token != null;
        headers.setBearerAuth(token);
        headers.set("Authorization", token);
        return token;
    }


    public String nodeUrl(String username) {
        System.out.println(username);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(bootstrapUrl.concat("/user/get-node-url?username=").concat(username), String.class);
        nodeUrl = responseEntity.getBody();
        return nodeUrl;
    }

    public String connectToDatabase(String dbName) {
        HttpEntity<String> entityReq = new HttpEntity<>(dbName, headers);
        return restTemplate.exchange(nodeUrl.concat("/user/connect-to-database?dbName=").concat(dbName), HttpMethod.GET, entityReq, String.class).getBody();
    }

    public void createDatabase(String dbName) {
        HttpEntity<String> entityReq = new HttpEntity<>(dbName, headers);
        restTemplate.exchange(nodeUrl.concat("/user/create-database?dbName=").concat(dbName),
                HttpMethod.POST, entityReq, String.class);
    }

    public void deleteDatabase(String dbName) {
        restTemplate.exchange(nodeUrl.concat("/user/delete-database"), HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);
    }

    //TODO, Replace all postEntity with exchange.
    public Collection createCollection(Collection collection) {
        ResponseEntity<Collection> responseEntity = restTemplate.postForEntity(nodeUrl.concat("/user/create-collection"), collection, Collection.class, headers);
        return responseEntity.getBody();
    }

    public void deleteCollection(Collection collection) {
        restTemplate.delete(nodeUrl.concat("/user/delete-collection"), collection, Collection.class, headers);
        restTemplate.exchange(nodeUrl.concat("/user/delete-collection"), HttpMethod.DELETE, new HttpEntity<>(collection, headers), Object.class);
    }

    public Document createRecord(Document document) {
        ResponseEntity<Document> responseEntity = restTemplate.postForEntity(nodeUrl.concat("/user/create-document"), document, Document.class, headers);
        return responseEntity.getBody();
    }

    public void deleteRecord(Document document) {

    }

    public Document modifyRecord() {
        return new Document();

    }

    public String getData(String collectionName, String prop, Object value) {
        ResponseEntity<Document> responseEntity = restTemplate.exchange(nodeUrl.concat("/user/find?collection=").concat(collectionName).concat("&").concat("prop=").concat(prop).concat("value=").concat(String.valueOf(value)), HttpMethod.GET, new HttpEntity<>(headers), Document.class);
        return Objects.requireNonNull(responseEntity.getBody()).getData().toJSONString();
    }

}
