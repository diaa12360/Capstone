package com.atypon.node.model;

import com.atypon.node.service.IndexService;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Database {
    public String connectedDatabase;
    public IndexService cash;
    public void setConnectedDatabase(String connectedDatabase) {
        cash.setDatabaseName(connectedDatabase);
        this.connectedDatabase = connectedDatabase;
    }
}
