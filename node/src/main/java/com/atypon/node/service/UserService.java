package com.atypon.node.service;

import com.atypon.node.exception.CollectionException;
import com.atypon.node.exception.DatabaseException;
import com.atypon.node.exception.DocumentException;
import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.model.MetadataFile;
import com.atypon.node.model.Node;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    private IndexCash indexCash;

    @Autowired
    public UserService(NodeService nodeService, RestTemplate restTemplate, IndexCash indexCash) {
        this.nodeService = nodeService;
        this.restTemplate = restTemplate;
        this.indexCash = indexCash;
        System.out.println("I'm Working NOW in UserServices!!");
    }

    public void connectToDatabase(String dbName) {
        try {
            JSONObject collections = (JSONObject) new JSONParser().parse(
                    new FileReader("Database/".concat(dbName).concat(File.separator).concat("metadata.json"))
            );
            indexCash = new IndexCash(dbName);
            for (String collection : (Set<String>) collections.keySet()) {
                indexCash.fillCollection((String) collections.get(collection), collection);
            }
        } catch (IOException e) {
            throw new DatabaseException("Database Dose NOT Exists");
        } catch (ParseException ignore) {
            //Ignore
        }
    }

    public Document find(String collectionName, String property, String value) {
        List<String> paths = indexCash.getPaths(collectionName, property, value);
        if (paths.isEmpty())
            throw new DocumentException("User Not Found");
        return Document.createUsingPath(paths.get(0));
    }

    public List<Document> findAll(String collectionName, String property, String value) {
        List<String> paths = indexCash.getPaths(collectionName, property, value);
        ArrayList<Document> documents = new ArrayList<>();
        for (String path : paths) {
            documents.add(Document.createUsingPath(path));
        }
        return documents;
    }

    public Document createDocument(Document document) {
        if (document.getDatabaseName() == null && indexCash.getDatabaseName() != null) {
            document.setDatabaseName(indexCash.getDatabaseName());
        }
        if (indexCash.getDatabaseName() == null || !document.getDatabaseName().equals(indexCash.getDatabaseName()))
            throw new DatabaseException("Please Connect to " + document.getDatabaseName() + " First!");
        //TODO,,,Collection Check
        String collectionPath = "Database" + document.getDatabaseName() + document.getCollectionName();
        MetadataFile metadata = new MetadataFile(collectionPath + "/metadata.json");
        JSONObject props = (JSONObject) metadata.readData().get("prop");
        for (String prop : (Set<String>) document.getData().keySet()) {
            if (props.containsKey(prop)) continue;
            throw new DocumentException("Please Enter The correct Data and properties!! like this ");
        }

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
        return document;
    }

    public Collection createCollection(Collection collection) {
        if (collection.getDatabaseName() == null)
            collection.setDatabaseName(indexCash.getDatabaseName());
        if (!collection.getDatabaseName().equals(indexCash.getDatabaseName()))
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
        if (indexCash.getIndex() == null || !document.getPath().contains(indexCash.getDatabaseName()))
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
    }

    //TODO, Reindexing
    public void deleteCollection(Collection collection) {
        if (!new File(collection.getPath()).exists())
            throw new CollectionException("Collection Dose NOT Exists");
        if (!collection.getDatabaseName().equals(indexCash.getDatabaseName()))
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
            //TODO
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
            //TODO
        }
    }

    //TODO Edit it And Create Unindexing Mechanisem
    public Document modifyDocument(Document documentAfter, Document documentBefore) {
        if (indexCash.getDatabaseName() == null)
            throw new DatabaseException("Please Connect to Database!!");
        if (!documentBefore.getDatabaseName().equals(indexCash.getDatabaseName()))
            throw new DatabaseException("Document Dose NOT Exists in \"" + indexCash.getDatabaseName() + "\" Database!!");
        File file = new File(documentBefore.getPath());
        if (file.canWrite()) {
            return nodeService.modifyDocumentForOthers(documentBefore, documentAfter);
        } else {
            HashMap<String, Document> mp = new HashMap<>();
            mp.put("documentAfter", documentAfter);
            mp.put("documentBefore", documentBefore);
            for (Node n : NodeService.getNodes()) {
                if (n.getId() != -1) {
                    Document document = restTemplate.postForEntity(
                            n.getAddress() + "node/modify-document",
                            mp,
                            Document.class
                    ).getBody();
                    if (document != null)
                        return documentAfter;
                }
            }
            return documentBefore;
        }
    }

    public List<String> getDatabases() {
        JSONParser parser = new JSONParser();
        JSONObject object = new JSONObject();
        try {
            object = (JSONObject) parser.parse(new FileReader("Database/allDBs.json"));
        } catch (IOException | ParseException e) {
            //ignored
        }
        return new ArrayList<String>(object.keySet());
    }

    public List<String> getCollections() {
        if (indexCash.getDatabaseName() == null)
            throw new DatabaseException("Please Connect To Database First!!");
        JSONParser parser = new JSONParser();
        JSONObject object = new JSONObject();
        try {
            String PATH = "Database/".concat(indexCash.getDatabaseName()).concat("/metadata.json");
            object = (JSONObject) parser.parse(new FileReader(PATH));
        } catch (IOException | ParseException e) {
            //ignored
        }
        return new ArrayList<String>(object.keySet());
    }

    public Document modifyDocument(Document after) {
        return modifyDocument(new Document(after), after);
    }
}
