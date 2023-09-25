package com.atypon.node.model;

import com.atypon.node.exception.DocumentException;
import com.atypon.node.service.ObjectIdGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Objects;

@Data
@NoArgsConstructor
public class Document implements Serializable {
    private String id;
    private JSONObject data;
    private String path;
    //TODO: Use It.
    private String collectionName;

    public Document(String path) {
        this.path = path;
    }

    public JSONObject read() {
        JSONObject allData = null;
        try {
            JSONParser parser = new JSONParser();
            allData = (JSONObject) parser.parse(new FileReader(path));
            id = (String) allData.get("_id");
            setData(allData);
        } catch (IOException e) {
            // if try to access nodeFile.json, and it did not create yet.
            if (path.contains("nodeFiles")) return allData;
            // if the file does not exist
            throw new DocumentException("Document Dose NOT Exist!!");
        } catch (ParseException ignored) {
            //Ignored!
        }
        return allData;
    }

    public JSONObject write(JSONObject newData) {
        try {
            JSONObject tempData = new JSONObject();
            String id = (String) data.get("_id");
            if (id != null) {
                data.put("_id", id);
            }
            File file = new File(getPath());
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel channel = raf.getChannel();
            FileLock lock = channel.lock();
            tempData.putAll(newData);
            FileWriter writer = new FileWriter(file);
            writer.write(tempData.toJSONString());
            writer.flush();
            writer.close();
            lock.release();
            channel.close();
            raf.close();
            data = newData;
        } catch (IOException e) {
            throw new DocumentException("File Locked Or Not Found");
        }
        return newData;
    }

    public JSONObject addData(String key, Object value) {
        JSONObject tempFile = read();
        tempFile.put(key, value);
        try {
            File file = new File(getPath());
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel channel = raf.getChannel();
            FileLock lock = channel.lock();
            FileWriter writer = new FileWriter(file);
            writer.write(tempFile.toJSONString());
            writer.flush();
            writer.close();
            lock.release();
            channel.close();
            read();
        } catch (IOException e) {
            throw new DocumentException("47");
        }
        return tempFile;
    }

    public JSONObject editData(String key, Object value) {
        return addData(key, value);
    }

    public void createFile(boolean amITheNode) {
        // if the user did not provide a _id for document
        if (id == null) {
            id = ObjectIdGenerator.getNewID();
            data.put("_id", id);
            path = path.concat(File.separator).concat(id).concat(".json");
        }
        else if (path.split("/").length < 4) { // if the user provide a _id for document put not in the path.
            path = path.concat(File.separator).concat(id).concat(".json");
        }
        File file = new File(path);
        // if the file is already exists.
        if (file.exists())
            throw new DocumentException("File Already exists!!!!");
        try {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(data.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new DocumentException("Check File path");
        }
        // if the node is the affinity node then set the file writable for it.
        if (!amITheNode) file.setWritable(false);
    }


    public String getDatabaseName() {
        return path.split("/")[1];
    }

    public String getCollectionName() {
        return path.split("/")[2];
    }

    @Override
    public boolean equals(Object o) {
        Document document = (Document) o;
        return Objects.equals(document.getPath(), getPath());
    }
}
