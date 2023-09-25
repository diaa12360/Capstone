package com.atypon.node.service;

import com.atypon.node.model.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Component
@Service
public class BootstrappingService {
    public void getInfo(@RequestBody String s) throws ParseException {
        if(!NodeService.getNodes().isEmpty()) {
            System.out.println("FUCKKKKK");
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

    public User addUser(@RequestBody User user) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject userJSON = user.getDataAsJSON();
        JSONObject object = (JSONObject) parser.parse(new FileReader("nodeFiles/users.json"));
        object.putAll(userJSON);
        System.out.println(user);
        FileWriter fileWriter = new FileWriter("nodeInfo.json");
        fileWriter.write(object.toJSONString());
        fileWriter.flush();
        fileWriter.close();
        return user;
    }

    private static File fileWithDirectoryAssurance(String directory, String filename) {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        return new File(directory + File.separator + filename);
    }
}
