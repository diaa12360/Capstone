package com.atypon.node.service;

import com.atypon.node.exception.DatabaseException;
import com.atypon.node.exception.DocumentException;
import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.model.Node;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;

@Component
@Service
public class NodeService {
    @Getter
    private static List<Node> nodes = updateAllNodes();
    @Getter
    private static Document nodeInfo = updatenNodeInfoFile();
    @Getter
    private static Document otherNodes = updateOtherNodesFile();
    public void createFile(Document document, String nodeName) {
        boolean amITheNode = nodeName.equals(nodeInfo.getData().get("name"));
        document.createFile(amITheNode);
        indexDocument(document);
        if (amITheNode) {
            nodeInfo.editData("affinity", (Long) nodeInfo.getData().get("affinity") + 1);
            nodeInfo.read();
        } else {
            Node affinity = null;
            for (Node n : nodes) {
                if (n.getName().equals(nodeName)) {
                    affinity = n;
                    break;
                }
            }
            JSONObject allNodesInFile = otherNodes.getData();
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
        if(file.exists())
            throw new DatabaseException("Database Is already Exists");
        file.mkdirs();
        file = new File(file.getPath().concat(File.separator).concat("metadata.json"));
        try {
            FileWriter writer = new FileWriter(file);
            writer.write("{}");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new DatabaseException("Check Create database");
        }
    }

    public void deleteDatabase(String dbName) {
        File file = new File("Database/" + dbName);
        if (!file.delete()) {
            throw new DatabaseException("Did not Deleted!!");
        }
    }

    public void deleteDocument(Document document) {
        File file = new File(document.getPath());
        if (!file.exists()) {
            throw new DocumentException("Document Dose NOT Exists!!");
        }
        if (file.canWrite()) {
            nodeInfo.editData("affinity", (Long) nodeInfo.getData().get("affinity") - 1);
            nodeInfo.read();
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
        if (file.setWritable(true) && !file.delete()) {
            throw new DocumentException("Can not Delete this Document!!");
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
        otherNodes.read();

    }

    public void deleteCollection(Collection collection) {
        String path = "Database/".concat(collection.getDatabaseName()).concat(File.separator).concat(collection.getName());
        File file = new File(path);
        if (!file.delete()) {
            throw new DatabaseException("Did not Deleted!!");
        }
        unIndexCollection(collection);
    }

    private void unIndexCollection(Collection collection) {
        JSONParser parser = new JSONParser();
        String dbPath = collection.getPath().substring(0, collection.getPath().lastIndexOf('/'));
        String metadataPath = dbPath.concat(File.separator).concat("/metadata.json");
        try {
            JSONObject metaData = (JSONObject) parser.parse(new FileReader(metadataPath));
            metaData.remove(collection.getName());
            FileWriter writer = new FileWriter(metadataPath);
            writer.write(metaData.toJSONString());
            writer.flush();
            writer.close();
        } catch (ParseException | IOException e) {

        }
    }

    public ResponseEntity<?> modifyDocumentForOthers(Document documentAfter, Document documentBefore) {
        File file = new File(documentAfter.getPath());
        if (file.canWrite()) {
            Document temp = new Document(documentBefore.getPath());
            temp.read();
            if (temp.getData().equals(documentBefore.getData())) {
                temp.write(documentAfter.getData());
                sendModification(documentAfter);
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>(HttpStatus.LOCKED);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private void sendModification(Document documentAfter) {
        //TODO Continue working on
    }

    private void unIndexDocument(Document document) {
        document.read();
        JSONObject data = document.getData();
        JSONParser parser = new JSONParser();
        String collectionPath = document.getPath().substring(0, document.getPath().lastIndexOf('/'));
        try {
            JSONObject props = (JSONObject) ((JSONObject)
                    parser.parse(
                            new FileReader(
                                    collectionPath.concat(File.separator).concat("/metadata.json")
                            )
                    )).get("prop");
            for (String prop : (Set<String>) props.keySet()) {
                Object value = data.get(prop);
                unIndex(document.getPath(), collectionPath, prop, value.toString());
            }
        } catch (ParseException | IOException e) {
            //TODO, Add Exception;
            System.out.println("Error here 174");
            e.printStackTrace();
        }
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
            index.put(value, arr);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(index.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (ParseException | IOException e) {
            System.out.println("Error here 206");
            e.printStackTrace();
        }
    }

    public void indexDocument(Document document) {
        JSONObject data = document.getData();
        JSONParser parser = new JSONParser();
        String collectionPath = (document.getPath().substring(0, document.getPath().lastIndexOf('/')));
        try {
            JSONObject props = (JSONObject) ((JSONObject)
                    parser.parse(
                            new FileReader(
                                    collectionPath.concat(File.separator).concat("metadata.json")
                            )
                    )).get("prop");
            for (String prop : (Set<String>) props.keySet()) {
                Object value = data.get(prop);
                index(document.getPath(), collectionPath, prop, String.valueOf(value));
            }
        } catch (ParseException | IOException e) {
            System.out.println("Error here 227");
            e.printStackTrace();
        }
    }

    public static List<Node> updateAllNodes() {
        List<Node> nodeList = new ArrayList<>();
        Document nodeInfo = new Document("nodeFiles/nodeInfo.json");
        Document otherNodes = new Document("nodeFiles/otherNodes.json");
        JSONObject thisNode = nodeInfo.read();
        JSONObject other = otherNodes.read();
        if(thisNode == null || other == null)
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
        //TODO, Get my Id from the bootstrabing node!!!
        nodeList.add(new Node(
                -1,
                (String) thisNode.get("name"),
                (String) thisNode.get("address"),
                (Long) thisNode.get("affinity")
        ));
        nodes = nodeList;
        return nodeList;
    }

    public static Document updatenNodeInfoFile() {
        Document document = new Document();
        document.setPath("nodeFiles/nodeInfo.json");
        File file = new File("nodeFiles");
        file.mkdir();
        if(document.read() == null)
            System.out.println("null");
        nodeInfo = document;
        return document;
    }

    public static Document updateOtherNodesFile() {
        Document document = new Document();
        document.setPath("nodeFiles/otherNodes.json");
        File file = new File("nodeFiles");
        file.mkdir();
        if(document.read() == null)
            System.out.println("null");
        otherNodes = document;
        return document;
    }
    //TODO, Create Index Database
}
