package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Collection {
    private String name;
    private String databaseName;
    private JSONObject prop;
}
