package com.atypon.node.service;

import com.atypon.node.model.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class BootstrappingService {
    public void getInfo(@RequestBody String s) throws ParseException {
        if(!NodeService.getNodes().isEmpty()) {
            return;
        }
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(s);
        FileWriter fileWriter = null;
        System.out.println(s);
        try {
            fileWriter = new FileWriter("nodeFiles/nodeInfo.json");
            JSONObject temp = (JSONObject)object.get("nodeInfo");
            fileWriter.write(temp.toJSONString());
            fileWriter.flush();
            fileWriter.close();
            
            fileWriter = new FileWriter("nodeFiles/otherNodes.json");
            temp = (JSONObject) object.get("otherNodes");
            fileWriter.write(temp.toJSONString());
            fileWriter.flush();
            fileWriter.close();

            fileWriter = new FileWriter("nodeFiles/users.json");
            temp = (JSONObject) object.get("users");
            fileWriter.write(temp.toJSONString());
            fileWriter.flush();
            fileWriter.close();

            NodeService.updatenNodeInfoFile();
            NodeService.updateOtherNodesFile();
            NodeService.updateAllNodes();
        } catch (IOException e) {
            //TODO, log or handle the exception
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    public User addUser(User user) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(new FileReader("nodeFiles/users.json"));
            JSONObject temp = new JSONObject();
            temp.put("password", user.getPassword());
            temp.put("role", user.getRole());
            temp.put("nodeAddress", user.getRole());
            object.put(user.getUsername(), temp);
            FileWriter fileWriter = new FileWriter("nodeFiles/users.json");
            fileWriter.write(object.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch (IOException | ParseException e){

        }
        return user;

    }

    private static File fileWithDirectoryAssurance(String directory, String filename) {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        return new File(directory + File.separator + filename);
    }
}
