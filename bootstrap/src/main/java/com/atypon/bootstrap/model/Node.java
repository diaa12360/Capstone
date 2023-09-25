package com.atypon.bootstrap.model;

import lombok.Data;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Data
public class Node {
    private String address;
    private String name;
    private List<User> usersList;
    private RestTemplate restTemplate;
    public Node(String address, String name){
        this.address = address;
        this.name = name;
    }
}
