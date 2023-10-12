package com.example.demo.repository;

import com.example.demo.model.AuthRequest;
import com.example.demo.model.Collection;
import com.example.demo.model.Document;
import com.example.demo.model.ParkingLot;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseUtil {
    private final RestTemplate restTemplate;
    @Value("${database.name}")
    private String dbName;
    private String collectionName;
    @Value("${database.url}")
    private String dbUrl;
    @Value("${database.username}")
    private String username;
    @Value("${database.password}")
    private String password;

    @Autowired
    public DatabaseUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void createAndConnect() {
        login();
        createDatabase();
        connectToDatabase();
    }

    private void login() {
        AuthRequest request = new AuthRequest(username, password);
        restTemplate.postForEntity(
                dbUrl.concat("/login"),
                request,
                String.class
        );
    }

    private void createDatabase() {
        try {
            restTemplate.postForEntity(
                    dbUrl.concat("/create-database?dbName=").concat(dbName),
                    "",
                    String.class
            );
        } catch (Exception e) {
            System.out.println("createDatabase");
        }
    }

    private void connectToDatabase() {
        try {
            restTemplate.getForEntity(
                    dbUrl.concat("/connect-to-database?dbName=").concat(dbName),
                    String.class,
                    ""
            );
        } catch (Exception e) {
            System.out.println("connectToDatabase");
        }
    }

    public void setCollectionName(String collectionName, JSONObject props) {
        this.collectionName = collectionName;
        createCollection(props);
    }

    private void createCollection(JSONObject props) {
        Collection collection = new Collection();
        collection.setName(collectionName);
        collection.setProp(props);
        collection.setDatabaseName(dbName);
        try {
            restTemplate.postForEntity(
                    dbUrl.concat("/create-collection?dbName=").concat(dbName),
                    collection,
                    Collection.class
            );
        } catch (Exception e) {
            System.out.println("Creating collection");
        }
    }

    public void save(JSONObject data) {
        Document document = new Document();
        document.setDatabaseName(dbName);
        document.setCollectionName(collectionName);
        document.setData(data);
        restTemplate.postForEntity(
                dbUrl.concat("/create-record"),
                document,
                Document.class
        );
    }

    public JSONObject findByID(String id) {
        Document document = restTemplate.getForObject(
                dbUrl.concat("/find?collectionName=").concat(collectionName)
                        .concat("&prop=id&value=").concat(String.valueOf(id)),
                Document.class,
                ""
        );
        return document.getData();
    }

    public List<JSONObject> findAllByID(String id) {
        List<Document> documents = restTemplate.getForObject(
                dbUrl.concat("/find-all?collectionName=").concat(collectionName)
                        .concat("&prop=id&value=").concat(String.valueOf(id)),
                List.class,
                ""
        );
        List<JSONObject> result = new ArrayList<>();

        for (Document document : documents) {
            result.add(document.getData());
        }
        return result;
    }

    public List<JSONObject> findAll() {
        Document[] documents = restTemplate.getForObject(
                dbUrl.concat("/find-all?collectionName=").concat(collectionName)
                        .concat("&prop=null&value=null"),
                Document[].class
        );
        List<JSONObject> result = new ArrayList<>();

        for (Document document : documents) {
            result.add(document.getData());
        }
        return result;
    }

    public void delete(Object o) {
        restTemplate.exchange(
                dbUrl.concat("create-document"),
                HttpMethod.DELETE,
                new HttpEntity<>(o),
                Object.class
        );
    }

    public Object modify(Object o){
        return restTemplate.exchange(
                dbUrl.concat("/user/modify-document"),
                HttpMethod.PUT,
                new HttpEntity<>(o),
                Document.class
        ).getBody();
    }
}
