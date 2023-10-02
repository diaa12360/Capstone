package com.atypon.bootstrap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table
@Data
public class User implements Serializable {
    @Id
    private String username;
    @Column
    private String password;
    @Column
    private String role;
    @Column
    private String nodeAddress;

    public User(){}

    public User(String username, String password, String nodeAddress) {
        this.username = username;
        this.password = password;
        this.nodeAddress = nodeAddress;
    }
}
