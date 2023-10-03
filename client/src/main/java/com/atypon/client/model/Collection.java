package com.atypon.client.model;

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
}
