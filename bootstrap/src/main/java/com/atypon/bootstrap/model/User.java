package com.atypon.bootstrap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table
@Data
public class User {
    @Id
    private Long id;
    @Column
    private String password;
    @Column
    private String nodeAddress;
    @Column
    private String role;

    public User(){}

    public User(Long id, String password, String nodeAddress) {
        this.id = id;
        this.password = password;
        this.nodeAddress = nodeAddress;
    }
}
