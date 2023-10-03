package com.atypon.node.exception;

import java.io.Serializable;

public class DatabaseException extends NodeException implements Serializable {
    public DatabaseException(String s) {
        super(s);
    }
}
