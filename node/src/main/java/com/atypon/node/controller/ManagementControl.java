package com.atypon.node.controller;

import com.atypon.node.model.User;
import com.atypon.node.service.BootstrappingService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping("/manage")
public class ManagementControl {

    private final BootstrappingService service;

    @Autowired
    public ManagementControl(BootstrappingService service) {
        this.service = service;
    }

    @PostMapping("/init")
    public ResponseEntity<?> getInfo(@RequestBody String s) throws ParseException {
        service.getInfo(s);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/add-user")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        User newUser = service.addUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

}
