package com.atypon.node.model;

import com.atypon.node.exception.CollectionException;
import com.atypon.node.exception.DocumentException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Collection {
    private String name;
    private String databaseName;
    private JSONObject prop;

    public void createCollection() {
        String path = "Database".concat(File.separator).concat(getDatabaseName()).concat(File.separator).concat(getName());
        File file = new File(path);
        if (!file.mkdir()) throw new CollectionException("Collection is Already Exist");
        addCollectionToDatabase(path);
        createMetaDataForCollection(path);
        createIndexFiles(path, getProp());
    }

    private void addCollectionToDatabase(String path) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject databaseMetadata = (JSONObject) parser.parse(new FileReader(
                    "Database".concat(File.separator).concat(getDatabaseName()).concat(File.separator).concat("metadata.json")
            ));
            databaseMetadata.put(getName(), path);
            FileWriter fileWriter = new FileWriter("Database".concat(File.separator).concat(getDatabaseName()).concat(File.separator).concat("metadata.json"));
            fileWriter.write(databaseMetadata.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            throw new DocumentException("Parsing Issue!!");
        } catch (IOException e) {
            throw new DocumentException("Meta Data is not There!!");
        }
    }

    private void createMetaDataForCollection(String path) {
        File metaData = new File(path.concat(File.separator).concat("metadata.json"));
        try {
            FileWriter fileWriter = new FileWriter(metaData);
            JSONObject object = new JSONObject();
            object.put("_name", getName());
            object.put("_database", getDatabaseName());
            object.put("prop", getProp());
            fileWriter.write(object.toJSONString());
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            throw new CollectionException("Collection dose not exist!!");
        }
    }

    public static void createIndexFiles(String path, JSONObject prop) {
        File file = new File(path.concat(File.separator).concat("index"));
        file.mkdir();
        for (String key : (Set<String>) prop.keySet()) {
            try {
                FileWriter fileWriter = new FileWriter(file.getPath().concat(File.separator).concat(key).concat(".json"));
                fileWriter.write("{}");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                throw new CollectionException("Can't Create Index for " + key + " Property");
            }
        }
    }

    public String getPath() {
        return "Database/".concat(databaseName).concat(File.separator).concat(name);
    }

    public String loadProps() {
        MetadataFile metadata = new MetadataFile(getPath() + "/metadata.json");
        File file = new File(getPath());
        if (!file.exists()) {
            throw new CollectionException("Collection NOT Exists in Database: \"" + databaseName + "\"");
        }
        setProp((JSONObject) metadata.readData().get("prop"));
        return prop.toJSONString();
    }

}
