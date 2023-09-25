package com.atypon.node.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Node {
    private int id;
    private String name;
    private String address;
    private long affinity;
}
