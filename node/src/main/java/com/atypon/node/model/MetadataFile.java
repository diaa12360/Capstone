package com.atypon.node.model;

import com.atypon.node.exception.DocumentException;
import lombok.Data;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

@Data
public class MetadataFile {
    private String path;
    private JSONObject data;

    public MetadataFile(String path) {
        this.path = path;
    }

    public JSONObject readData() {
        try (FileReader reader = new FileReader(path)) {
            JSONParser parser = new JSONParser();
            data = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
            return null;
        } catch (ParseException ignored) {
            //Ignored!
        }
        return data;
    }

    public void editData(String affinityId, Object affinityNode) {
        JSONObject tempFile = readData();
        tempFile.put(affinityId, affinityNode);
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
            readData();
        } catch (IOException e) {
            throw new DocumentException("47");
        }
    }

    public void write(JSONObject newData) {
        try {
            JSONObject tempData = new JSONObject();
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
            readData();
        } catch (IOException e) {
            throw new DocumentException("File Locked Or Not Found");
        }
    }
}
