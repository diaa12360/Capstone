package com.atypon.node.service;

import com.atypon.node.exception.CollectionException;
import com.atypon.node.exception.DatabaseException;
import com.atypon.node.exception.DocumentException;
import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.model.Node;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
@Service
public class UserService {
    private final NodeService nodeService;
    private final RestTemplate restTemplate;
    private IndexService indexService;

    @Autowired
    public UserService(NodeService nodeService, RestTemplate restTemplate, IndexService indexService) {
        this.nodeService = nodeService;
        this.restTemplate = restTemplate;
        this.indexService = indexService;
        System.out.println("I'm Working NOW in UserServices!!");
    }

    public void connectToDatabase(String dbName) {
        try {
            JSONObject collections = (JSONObject) new JSONParser().parse(
                    new FileReader("Database/".concat(dbName).concat(File.separator).concat("metadata.json"))
            );
            indexService = new IndexService(dbName);
            for (String collection: (Set<String>) collections.keySet()){
                indexService.fillCollection((String) collections.get(collection), collection);
            }
        } catch (IOException e) {
            throw new DatabaseException("Database Dose NOT Exists");
        }catch (ParseException e){

        }
    }

    public Document find(String collectionName, String property, String value){
        Document document = new Document();
        List<String> paths = indexService.getData(collectionName, property, value);
        if(paths.isEmpty())
            return null;
        document.setPath(paths.get(0));
        document.read();
        return document;
    }

    public List<Document> findAll(String collectionName, String property, String value){
        List<String> paths = indexService.getData(collectionName, property, value);
        ArrayList<Document> documents = new ArrayList<>();
        for (String path: paths) {
            Document document = new Document();
            document.setPath(path);
            document.read();
            documents.add(document);
        }
        return documents;
    }

    public Document createDocument(Document document) {
        if (!document.getPath().contains(indexService.getDatabaseName()))
            throw new DatabaseException("Please Connect to " + document.getDatabaseName() + " First!");
        Node minAffinity = NodeService.getNodes().get(0);
        for (Node n : NodeService.getNodes()) {
            if (n.getAffinity() < minAffinity.getAffinity()) {
                minAffinity = n;
            }
        }
        nodeService.createFile(document, minAffinity.getName());
        for (Node n : NodeService.getNodes()) {
            if (n.getId() != -1)
                restTemplate.postForEntity(
                        n.getAddress() + "node/create-document?name=" + minAffinity.getName(),
                        document,
                        Document.class
                );
        }
        minAffinity.setAffinity(minAffinity.getAffinity() + 1);
        nodeService.createFile(document, minAffinity.getName());
        indexService.indexDocument(document);
        return document;
    }

    public Collection createCollection(Collection collection) {
        if (!collection.getDatabaseName().equals(indexService.getDatabaseName()))
            throw new DatabaseException("Please Connect to " + collection.getDatabaseName() + " First!");
        for (Node n : NodeService.getNodes()) {
            if (n.getId() != -1)
                restTemplate.postForEntity(
                        n.getAddress() + "node/create-collection",
                        collection,
                        Collection.class
                );
        }
        nodeService.createCollection(collection);
        return collection;
    }

    public void createDatabase(String dbName) {
        if (new File("Database/" + dbName).exists())
            throw new DatabaseException("Database Already Exists");
        nodeService.createDatabase(dbName);
        for (Node n : NodeService.getNodes()) {
            if (n.getId() != -1)
                restTemplate.postForEntity(
                        n.getAddress().concat("node/create-database?dbName=").concat(dbName),
                        "", String.class
                );
        }
    }

    public void deleteDatabase(String dbName) {
        if (!new File("Database/" + dbName).exists())
            throw new DatabaseException("Database Dose NOT Exists");
        for (Node n : NodeService.getNodes()) {
            if (n.getId() != -1)
                restTemplate.delete(
                        n.getAddress().concat("node/delete-database?dbName=").concat(dbName)
                );
        }
        nodeService.deleteDatabase(dbName);
    }

    public void deleteDocument(Document document) {
        System.out.println(document.getPath());
        if (!new File(document.getPath()).exists())
            throw new DocumentException("Document Dose NOT Exists");
        if (indexService.getIndex() == null || !document.getPath().contains(indexService.getDatabaseName()))
            throw new DatabaseException("Please Connect to " + document.getDatabaseName() + " First!");
        HttpEntity<Document> request = new HttpEntity<>(document);
        for (Node n : NodeService.getNodes()) {
            if (n.getId() != -1)
                restTemplate.exchange(
                        n.getAddress() + "/node/delete-document",
                        HttpMethod.DELETE,
                        request, Document.class
                );
        }
        nodeService.deleteDocument(document);
        indexService.unIndexDocument(document);
    }

    public void deleteCollection(Collection collection) {
        if (!new File(collection.getPath()).exists())
            throw new CollectionException("Collection Dose NOT Exists");
        if (!collection.getDatabaseName().equals(indexService.getDatabaseName()))
            throw new DatabaseException("Please Connect to " + collection.getDatabaseName() + " First!");
        for (Node n : NodeService.getNodes()) {
            if (n.getId() != -1)
                restTemplate.delete(
                        n.getAddress().concat("node/delete-collection"),
                        collection, Collection.class
                );
        }
        nodeService.deleteCollection(collection);
    }

    private void unIndex(String documentPath, String collectionPath, String prop, String value) {
        JSONParser parser = new JSONParser();
        try {
            File file = new File(collectionPath.concat(File.separator).concat("index").
                    concat(File.separator).concat(prop).concat(".json"));
            JSONObject index = (JSONObject) parser.parse(new FileReader(file));
            index.remove(value, documentPath);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(index.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }


    public void index(String documentPath, String collectionPath, String prop, String value) {
        JSONParser parser = new JSONParser();
        try {
            File file = new File(collectionPath.concat(File.separator).concat("index").concat(File.separator).concat(prop).concat(".json"));
            JSONObject index = (JSONObject) parser.parse(new FileReader(file));
            JSONArray arr = (JSONArray) index.getOrDefault(value, new JSONArray());
            arr.add(documentPath);
            index.put(value, documentPath);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(index.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (ParseException | IOException e) {
            System.out.println("Error here 206");
            e.printStackTrace();
        }
    }

    //TODO Edit it And Create Unindexing Mechanisem
    public ResponseEntity<?> modifyDocument(Document documentAfter, Document documentBefore) {
        if (nodeService.modifyDocumentForOthers(documentAfter, documentBefore).getStatusCode().equals(HttpStatus.OK)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            RestTemplate restTemplate = new RestTemplate();
            HashMap<String, Document> mp = new HashMap<>();
            mp.put("documentAfter", documentAfter);
            mp.put("documentBefore", documentBefore);
            for (Node n : NodeService.getNodes()) {
                if (n.getId() != -1 &&
                        (restTemplate.postForEntity(n.getAddress() + "node/modify-document",
                                mp, HashMap.class).getStatusCode().equals(HttpStatus.ACCEPTED))) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    //TODO, Fill These Methods
    public List<String> getDatabases() {
        return new ArrayList<>();
    }

    public List<String> getCollections() {
        return new ArrayList<>();
    }

    //TODO, getDatabases(), getCollections()

}
