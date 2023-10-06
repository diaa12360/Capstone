package com.atypon.node.service;

import com.atypon.node.exception.CollectionException;
import com.atypon.node.exception.DatabaseException;
import com.atypon.node.exception.DocumentException;
import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.model.MetadataFile;
import com.atypon.node.model.Node;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileReader;
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
    private final IndexCash indexCash;

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
            indexCash.connect(dbName);
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
        if (paths == null || paths.isEmpty())
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

    private boolean propsIsOk(Document document) {
        Set<String> propsInCollection = document.readProps().keySet();
        Set<String> propsInData = document.getData().keySet();
        return propsInCollection.containsAll(propsInData);
    }

    public Document createDocument(Document document) {
        if (document.getDatabaseName() == null && indexCash.getDatabaseName() != null) {
            document.setDatabaseName(indexCash.getDatabaseName());
        }
        if (indexCash.getDatabaseName() == null || !document.getDatabaseName().equals(indexCash.getDatabaseName()))
            throw new DatabaseException("Please Connect to " + document.getDatabaseName() + " First!");
        if(!propsIsOk(document)){
            throw new DocumentException("You Entering Wrong Properties Please Check The Right One And Try Again!!");
        }
        String collectionPath = "Database/" + document.getDatabaseName() + File.separator + document.getCollectionName();
        MetadataFile metadata = new MetadataFile(collectionPath + "/metadata.json");
        JSONObject props = (JSONObject) metadata.readData().get("prop");
        for (String prop : (Set<String>) document.getData().keySet()) {
            if (props.containsKey(prop)) continue;
            throw new DocumentException("Please Enter The correct Data and properties!! like this ....");
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
            String path = "Database/".concat(indexCash.getDatabaseName()).concat("/metadata.json");
            object = (JSONObject) parser.parse(new FileReader(path));
        } catch (IOException | ParseException e) {
            //ignored
        }
        return new ArrayList<>(object.keySet());
    }

    public Document modifyDocument(Document after) {
        Document currentDocument = Document.createUsingPath(after.getPath());
        return modifyDocument(after, currentDocument);
    }

    public String getProps(String collectionName) {
        Collection collection = new Collection();
        if (indexCash.getDatabaseName() == null) {
            throw new DatabaseException("Please Connect to Database First!!");
        }
        collection.setDatabaseName(indexCash.getDatabaseName());
        collection.setName(collectionName);
        return collection.loadProps();
    }
}
