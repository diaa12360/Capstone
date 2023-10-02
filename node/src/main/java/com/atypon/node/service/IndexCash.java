package com.atypon.node.service;

import com.atypon.node.exception.CollectionException;
import com.atypon.node.exception.DatabaseException;
import com.atypon.node.exception.DocumentException;
import com.atypon.node.model.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
@Service
@NoArgsConstructor
@AllArgsConstructor
@Data
public class IndexCash {
    private String databaseName;
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> index;

    public IndexCash(String databaseName) {
        this.databaseName = databaseName;
        index = new HashMap<>();
    }

    public void fillCollection(String collectionPath, String collectionName) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject props = (JSONObject) ((JSONObject) parser.parse(new FileReader(
                    collectionPath.concat(File.separator).concat("metadata.json")))).get("prop");
            HashMap<String, HashMap<String, ArrayList<String>>> innerMap = new HashMap<>();
            for (String prop : (Set<String>) props.keySet()) {
                JSONObject propIndex = (JSONObject) parser.parse(new FileReader(
                        collectionPath.concat(File.separator).concat("index")
                                .concat(File.separator).concat(prop).concat(".json")));

                innerMap.put(prop, propIndex);
            }
            index.put(collectionName, innerMap);
        } catch (IOException e) {
            throw new CollectionException("Collection Does NOT Exists!!");
        } catch (ParseException e) {
            throw new CollectionException("Parsing E");
        }
    }

    public List<String> getData(String collection, String prop, String value) {
        if (index == null)
            throw new DatabaseException("Please Connect to Database!!");
        else if (index.get(collection) == null)
            throw new CollectionException("Collection:" + collection + " Dose NOT exists In " + databaseName + "!!");
        else if (index.get(collection).get(prop) == null)
            throw new CollectionException("Collection:" + collection + " Dose NOT have " + prop + " Property!!");
        return index.get(collection).get(prop).get(value);
    }

    private void addRecord(String dbName, String collectionName, String prop, String value, String documentPath) {
        if (!dbName.equals(databaseName))
            throw new DatabaseException("This is not the Database You Connect!!");
        if (!new File(documentPath).exists())
            throw new DocumentException("There is NO DATA!!");

        HashMap<String, HashMap<String, ArrayList<String>>> inner = index.get(collectionName);
        if (!new File("Database/" + dbName + File.separator + collectionName).exists()) {
            throw new CollectionException("Collection Does NOT exists");
        }
        if (inner == null) {
            inner = new HashMap<>();
        }
        HashMap<String, ArrayList<String>> thirdMap = inner.computeIfAbsent(prop, k -> new HashMap<>()); // if the Inner Does NOT hashMap exist create new one.
        ArrayList<String> paths = thirdMap.get(value);
        if (paths == null)
            paths = new ArrayList<>();
        paths.add(documentPath);
    }

    private void deleteRecord(String dbName, String collectionName, String prop, String value, String documentPath) {
        if (!dbName.equals(databaseName))
            throw new DatabaseException("This is not the Database You Connect!!");
        if (!new File(documentPath).exists())
            throw new DocumentException("There is NO DATA!!");
        HashMap<String, HashMap<String, ArrayList<String>>> inner = index.get(collectionName);
        if (inner == null || !new File("Database/" + dbName + File.separator + collectionName).exists()) {
            throw new CollectionException("Collection Does NOT exists");
        }
        HashMap<String, ArrayList<String>> thirdMap = inner.get(prop);
        if (thirdMap == null)
            throw new DocumentException("Document Does NOT exists");
        thirdMap.get(value).remove(documentPath);
    }

    public void unIndexDocument(Document document) {
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
                deleteRecord(document.getDatabaseName(), document.getCollectionName(), prop, String.valueOf(value), document.getPath());
            }
        } catch (ParseException | IOException e) {
            //TODO: Add Exception;
            System.out.println("Error here 174");
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
                addRecord(document.getDatabaseName(), document.getCollectionName(), prop, String.valueOf(value), document.getPath());
            }
        } catch (ParseException | IOException e) {
            System.out.println("Error here 227");
        }
    }

}
