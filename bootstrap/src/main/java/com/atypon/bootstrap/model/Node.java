package com.atypon.bootstrap.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Node {
    private String address;
    private String name;
    private List<User> usersList;
    private String token;
    public Node(String address, String name, List<User> usersList){
        this.address = address;
        this.name = name;
        this.usersList = usersList;
    }
}
