package com.atypon.node.controller;

import com.atypon.node.exception.DatabaseException;
import com.atypon.node.exception.NodeException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SerializationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(NodeException.class)
    public ResponseEntity<?> handleDBException(NodeException e) {
        // log exception
        JSONObject object = new JSONObject();
        object.put("type", e.getClass().getName());
        object.put("message", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(object.toJSONString());
    }
}
