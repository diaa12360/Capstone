package com.atypon.node.model;

import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class User {
    private long id;
    private String password;
    private String role;

    public JSONObject getDataAsJSON(){
        JSONObject jsonObject = new JSONObject();
        JSONObject temp = new JSONObject();
        temp.put("password", password);
        temp.put("role", role);
        jsonObject.put(id, temp);
        return jsonObject;
    }
}
