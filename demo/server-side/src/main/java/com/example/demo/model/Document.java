package com.example.demo.model;

import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class Document {
    private String id;
    private JSONObject data;
    private String collectionName;
    private String databaseName;
}
