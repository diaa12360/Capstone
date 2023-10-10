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
import java.nio.channels.OverlappingFileLockException;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
public class Document implements Serializable {
    private String id;
    private JSONObject data;
    private String collectionName;
    private String databaseName;

    public Document(Document document) {
        this.id = document.getId();
        this.databaseName = document.getDatabaseName();
        this.collectionName = document.getCollectionName();
        this.data = document.getData();
    }

    public static Document createUsingPath(String path) {
        String[] arr = path.split("/");
        Document document = new Document();
        document.setId(arr[3].substring(0, arr[3].lastIndexOf(".")));
        document.setCollectionName(arr[2]);
        document.setDatabaseName(arr[1]);
        document.read();
        return document;
    }


    public JSONObject read() {
        JSONObject allData = null;
        try {
            data = new JSONObject();
            JSONParser parser = new JSONParser();
            String path = getPath();
            allData = (JSONObject) parser.parse(
                    new FileReader(path)
            );
            setData(allData);
        } catch (IOException e) {
            // if try to access nodeFile.json, and it did not create yet.
            if (getPath().contains("nodeFiles")) return allData;
            throw new DocumentException("Document Dose NOT Exist!!");
        } catch (ParseException ignored) {
            //Ignored!
        }
        return allData;
    }

    public JSONObject write(JSONObject newData) {
        try {
            JSONObject tempData = new JSONObject();
            // TODO, I don't know!
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
        if (readProps().get("key") == null) {
            throw new DocumentException("The Property: \"" + key + "\" Does NOT Exists in This Collection!");
        }
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


    public void createFile(boolean amITheNode) {
        // if the user did not provide a _id for document
        String path = getPath();
        File file = new File(path);
        // if the file is already exists.
        if (file.exists())
            throw new DocumentException("Document With ID: " + id + " is Already exists!!!!");
        //prepare Data for null.
        fillWithNullIfNoData();
        try (FileWriter fileWriter = new FileWriter(path);) {
            fileWriter.write(data.toJSONString());
            fileWriter.flush();
        } catch (IOException e) {
            throw new DocumentException("Check File path");
        }
        // if the node is the affinity node then set the file writable for it.
        if (!amITheNode) {
            try {
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                FileChannel channel = raf.getChannel();
                channel.lock();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            file.setWritable(false);
        }
    }

    private void fillWithNullIfNoData() {
        JSONObject props = readProps();
        for (String s : (Set<String>) props.keySet()) {
            data.putIfAbsent(s, null);
        }
    }


    public String getPath() {
        if (id == null)
            id = ObjectIdGenerator.getNewID();
        for (String key : (Set<String>) readProps().keySet()) {
            if (key.equalsIgnoreCase("id")) {
                if (data.get(key) == null) {
                    data.put(key, id);
                } else {
                    id = String.valueOf(data.get(key));
                }
                break;
            }
        }

        return "Database".concat(File.separator)
                .concat(databaseName).concat(File.separator)
                .concat(collectionName).concat(File.separator)
                .concat(id).concat(".json");
    }

    @Override
    public boolean equals(Object o) {
        Document document = (Document) o;
        return Objects.equals(document.getPath(), getPath());
    }

    public JSONObject readProps() {
        String collectionPath = "Database/" + getDatabaseName().concat(File.separator).concat(getCollectionName());
        MetadataFile metadata = new MetadataFile(collectionPath + "/metadata.json");
        return (JSONObject) metadata.readData().get("prop");
    }

    public boolean canWrite() {
        boolean canWrite = false;
        try {
            RandomAccessFile raf = new RandomAccessFile(getPath(), "rw");
            FileChannel channel = raf.getChannel();
            FileLock lock = null;
            try {
                lock = channel.tryLock();
                if (lock != null) {
                    canWrite = true;
                    lock.release(); // Release the lock
                }
            } catch (OverlappingFileLockException e) {
                canWrite = false;
            }
        } catch (FileNotFoundException e) {
            throw new DocumentException("Document Not Found!");
        } catch (IOException e) {
            throw new DocumentException("Something Wrong");
        }
        return canWrite && new File(getPath()).canWrite();
    }
}