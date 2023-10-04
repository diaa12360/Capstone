package com.atypon.node.service;

import com.atypon.node.exception.DatabaseException;
import com.atypon.node.exception.DocumentException;
import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.model.MetadataFile;
import com.atypon.node.model.Node;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
@Service

public class NodeService {
    @Getter
    private static List<Node> nodes = updateAllNodes();
    @Getter
    private static MetadataFile nodeInfo = updateNodeInfoFile();
    @Getter
    private static MetadataFile otherNodes = updateOtherNodesFile();

    private final IndexCash indexCash;

    @Autowired
    public NodeService(IndexCash indexCash) {
        this.indexCash = indexCash;
    }

    public void createFile(Document document, String nodeName) {
        boolean amITheNode = nodeName.equals(nodeInfo.readData().get("name"));
        document.createFile(amITheNode);
        indexDocument(document);
        if (amITheNode) {
            nodeInfo.editData("affinity", (Long) nodeInfo.getData().get("affinity") + 1);
        } else {
            Node affinity = null;
            for (Node n : nodes) {
                if (n.getName().equals(nodeName)) {
                    affinity = n;
                    break;
                }
            }
            if (affinity == null)
                throw new DatabaseException("Unexpected Error");
            JSONObject allNodesInFile = otherNodes.readData();
            for (String key : (Set<String>) allNodesInFile.keySet()) {
                JSONObject nodeData = (JSONObject) allNodesInFile.get(key);
                if (nodeData.get("name").equals(nodeName)) {
                    nodeData.put("affinity", affinity.getAffinity() + 1);
                }
            }
            otherNodes.write(allNodesInFile);
        }
    }

    public void createCollection(Collection collectionData) {
        collectionData.createCollection();
    }

    public void createDatabase(String name) {
        File file = new File("Database".concat(File.separator).concat(name));
        if (file.exists())
            throw new DatabaseException("Database Is already Exists");
        file.mkdirs();
        addDatabaseToJSONFile(name);
        file = new File(file.getPath().concat(File.separator).concat("metadata.json"));
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{}");
            writer.flush();
        } catch (IOException e) {
            throw new DatabaseException("Check Create database");
        }
    }

    private void addDatabaseToJSONFile(String dbName) {
        JSONParser parser = new JSONParser();
        JSONObject object = new JSONObject();
        try {
            object = (JSONObject) parser.parse(new FileReader("Database/allDBs.json"));
        } catch (IOException e) {
            //TODO
        } catch (ParseException e) {
            //TODO
        }
        object.put(dbName, "Database/" + dbName);
        try (FileWriter writer = new FileWriter("Database/allDBs.json")) {
            writer.write(object.toJSONString());
            writer.flush();
        } catch (IOException ignored) {
            //ignored
        }
    }

    //TODO unIndex
    public void deleteDatabase(String dbName) {
        try {
            Files.delete(Path.of("Database/" + dbName));
        } catch (IOException e) {
            throw new DatabaseException("Did not Deleted");
        }
    }

    public void deleteDocument(Document document) {
        File file = new File(document.getPath());
        if (!file.exists()) {
            throw new DocumentException("Document Dose NOT Exists!!");
        }
        // decrease affinity and send the new one to the other nodes
        if (file.canWrite()) {
            nodeInfo.editData("affinity", (Long) nodeInfo.getData().get("affinity") - 1);
            RestTemplate restTemplate = new RestTemplate();
            for (Node n : nodes) {
                if (n.getId() != -1) {
                    restTemplate.postForEntity(
                            n.getAddress() + "node/decrease-affinity?nodeName=" + nodeInfo.getData().get("name"),
                            "", String.class
                    );
                    n.setAffinity(n.getAffinity() - 1);
                }
            }
        }
        unIndexDocument(document);
        if (file.setWritable(true)) {
            try {
                Files.delete(Path.of(file.getPath()));
            } catch (IOException e) {
                throw new DocumentException("Can not Delete this Document!!");
            }
        }
    }

    public void decreaseAffinity(String nodeName) {
        JSONObject nodesData = otherNodes.getData();
        String affinityId = "";
        for (Node n : nodes) {
            if (Objects.equals(n.getName(), nodeName)) {
                affinityId = String.valueOf(n.getId());
                n.setAffinity(n.getAffinity() - 1);
                break;
            }
        }
        JSONObject affinityNode = (JSONObject) nodesData.get(affinityId);
        affinityNode.put("affinity", (Long) affinityNode.get("affinity") - 1);
        otherNodes.editData(affinityId, affinityNode);

    }

    public void deleteCollection(Collection collection) {
        String path = "Database/".concat(collection.getDatabaseName()).concat(File.separator).concat(collection.getName());
        File file = new File(path);
        unIndexCollection(collection);
        if (file.setWritable(true)) {
            try {
                Files.delete(Path.of(file.getPath()));
            } catch (IOException e) {
                throw new DatabaseException("Collection Did not Deleted!!");
            }
        }
    }

    private void unIndexCollection(Collection collection) {
        JSONParser parser = new JSONParser();
        String dbPath = collection.getPath().substring(0, collection.getPath().lastIndexOf('/'));
        String metadataPath = dbPath.concat(File.separator).concat("/metadata.json");
        try (FileWriter writer = new FileWriter(metadataPath);
             FileReader reader = new FileReader(metadataPath);) {
            JSONObject metaData = (JSONObject) parser.parse(reader);
            metaData.remove(collection.getName());
            writer.write(metaData.toJSONString());
            writer.flush();
            indexCash.unIndexCollection(collection);
        } catch (ParseException | IOException e) {
            //TODO
        }
    }

    public Document modifyDocumentForOthers(Document documentBefore, Document documentAfter) {
        File file = new File(documentAfter.getPath());
        if (file.canWrite()) {
            Document temp = new Document(documentBefore);
            temp.read();
            if (temp.getData().equals(documentBefore.getData())) {
                Document document = modify(documentBefore, documentAfter);
                sendModification(documentBefore, documentAfter);
                return document;
            } else {
                throw new DocumentException("Your data are NOT Updated, Try Again!!");
            }
        }
        return null;
    }

    //TODO, Implement this
    private void sendModification(Document before, Document after) {
        HashMap<String, Document> mp = new HashMap<>();
        mp.put("after", after);
        mp.put("before", before);
        RestTemplate restTemplate = new RestTemplate();
        for (Node n : NodeService.getNodes()) {
            if (n.getId() != -1) {
                restTemplate.postForEntity(
                        n.getAddress() + "node/modify",
                        mp,
                        Document.class
                ).getBody();
            }
        }
    }

    public Document modify(Document before, Document after) {
        before.write(after.getData());
        reIndexDocument(before, after);
        after.read();
        return after;
    }

    private void reIndexDocument(Document before, Document after) {
        JSONObject props = before.readProps();
        JSONObject dataBefore = before.getData();
        JSONObject dataAfter = after.getData();
        for (String prop : (Set<String>) props.keySet()) {
            Object value1 = dataBefore.get(prop);
            Object value2 = dataAfter.get(prop);
            if (value1.equals(value2)) continue;
            unIndex(before, prop, value1.toString());
            index(after, prop, value1.toString());
        }

    }

    private void unIndexDocument(Document document) {
        document.read();
        JSONObject data = document.getData();
        JSONObject props = document.readProps();
        for (String prop : (Set<String>) props.keySet()) {
            Object value = data.get(prop);
            unIndex(document, prop, value.toString());
        }
    }

    private void unIndex(Document document, String prop, String value) {
        JSONParser parser = new JSONParser();
        File file = new File(
                document.getPath().substring(0, document.getPath().lastIndexOf("/"))
                        .concat(File.separator).concat("index").concat(File.separator)
                        .concat(prop).concat(".json"));
        try (FileWriter writer = new FileWriter(file);
             FileReader reader = new FileReader(file)) {
            JSONObject index = (JSONObject) parser.parse(reader);
            index.remove(value, document.getPath());
            writer.write(index.toJSONString());
            writer.flush();
            if (indexCash.getDatabaseName() != null && indexCash.getDatabaseName().equals(document.getPath()))
                indexCash.deleteRecord(
                        document.getDatabaseName(),
                        document.getCollectionName(),
                        prop,
                        value,
                        document.getPath()
                );
        } catch (ParseException | IOException ignored) {
            //Ignored
        }
    }


    public void index(Document document, String prop, String value) {
        JSONParser parser = new JSONParser();
        File file = new File(
                document.getPath().substring(0, document.getPath().lastIndexOf("/"))
                        .concat(File.separator).concat("index").concat(File.separator)
                        .concat(prop).concat(".json"));
        try (FileWriter writer = new FileWriter(file);
             FileReader reader = new FileReader(file);)  {
            JSONObject index = (JSONObject) parser.parse(reader);
            JSONArray arr = (JSONArray) index.getOrDefault(value, new JSONArray());
            arr.add(document.getPath());
            index.put(value, arr);
            writer.write(index.toJSONString());
            writer.flush();
            if (indexCash.getDatabaseName() != null && indexCash.getDatabaseName().equals(document.getDatabaseName()))
                indexCash.addRecord(
                        document.getDatabaseName(),
                        document.getCollectionName(),
                        prop,
                        value,
                        document.getPath()
                );
        } catch (ParseException | IOException e) {
            System.out.println("Error here 206");
        }
    }

    public void indexDocument(Document document) {
        JSONObject data = document.getData();
        JSONObject props = document.readProps();
        for (String prop : (Set<String>) props.keySet()) {
            Object value = data.get(prop);
            index(document, prop, String.valueOf(value));
        }
    }

    public static List<Node> updateAllNodes() {
        List<Node> nodeList = new ArrayList<>();
        MetadataFile nodeInfo = new MetadataFile("nodeFiles/nodeInfo.json");
        MetadataFile otherNodes = new MetadataFile("nodeFiles/otherNodes.json");
        JSONObject thisNode = nodeInfo.readData();
        JSONObject other = otherNodes.readData();
        if (thisNode == null || other == null)
            return nodeList;
        for (String key : (Set<String>) other.keySet()) {
            JSONObject nodeData = (JSONObject) other.get(key);
            nodeList.add(new Node(
                    Integer.parseInt(key),
                    (String) nodeData.get("name"),
                    (String) nodeData.get("address"),
                    (Long) nodeData.get("affinity")
            ));
        }
        nodeList.add(new Node(
                -1,
                (String) thisNode.get("name"),
                (String) thisNode.get("address"),
                (Long) thisNode.get("affinity")
        ));
        nodes = nodeList;
        return nodeList;
    }

    public static MetadataFile updateNodeInfoFile() {
        MetadataFile temp = new MetadataFile("nodeFiles/nodeInfo.json");
        temp.readData();
        File file = new File("nodeFiles");
        file.mkdir();
        if (temp.readData() == null)
            System.out.println("null");
        nodeInfo = temp;
        return temp;
    }

    public static MetadataFile updateOtherNodesFile() {
        MetadataFile document = new MetadataFile("nodeFiles/otherNodes.json");
        document.readData();
        File file = new File("nodeFiles");
        file.mkdir();
        if (document.readData() == null)
            System.out.println("null");
        otherNodes = document;
        return document;
    }
    //TODO, Create Index Database
}
