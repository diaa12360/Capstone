package com.atypon.node.model;

import lombok.Data;
import org.json.simple.JSONObject;

import java.io.Serializable;

@Data
public class User implements Serializable {
    private String username;
    private String password;
    private String role;
    private String nodeAddress;


    public JSONObject getDataAsJSON(){
        JSONObject jsonObject = new JSONObject();
        JSONObject temp = new JSONObject();
        temp.put("password", password);
        temp.put("role", role);
        jsonObject.put(username, temp);
        return jsonObject;
    }
}
