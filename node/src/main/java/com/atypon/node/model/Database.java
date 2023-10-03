package com.atypon.node.model;

import com.atypon.node.service.IndexCash;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Database {
    public String connectedDatabase;
    public IndexCash cash;
    public void setConnectedDatabase(String connectedDatabase) {
        cash.setDatabaseName(connectedDatabase);
        this.connectedDatabase = connectedDatabase;
    }
}
