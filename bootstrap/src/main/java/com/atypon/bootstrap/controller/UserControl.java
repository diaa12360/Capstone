package com.atypon.bootstrap.controller;

import com.atypon.bootstrap.model.User;
import com.atypon.bootstrap.resourses.Encryption;
import com.atypon.bootstrap.resourses.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserControl {

    private final UserService userService;

    @Autowired
    public UserControl(UserService userService) {
        this.userService = userService;
    }

    //TODO ADD ROLE
    @PostMapping("/create-account")
    public ResponseEntity<User> createAccount(@RequestBody User userRequest) {
        userRequest.setPassword(Encryption.encrypt(userRequest.getPassword()));
        User user = userService.createAccount(userRequest);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/get-node-url")
    public ResponseEntity<String> getUser(@RequestParam String username){
        return ResponseEntity.ok(userService.getNodeUrl(username));
    }
}
