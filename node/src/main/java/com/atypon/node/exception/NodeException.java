package com.atypon.node.exception;

import java.io.Serializable;

public abstract class NodeException extends RuntimeException implements Serializable {
    protected NodeException(String message){
        super(message);
    }
}
