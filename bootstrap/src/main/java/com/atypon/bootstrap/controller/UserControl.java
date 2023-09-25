package com.atypon.bootstrap.controller;

import com.atypon.bootstrap.model.Node;
import com.atypon.bootstrap.model.User;
import com.atypon.bootstrap.resourses.UserService;
import org.json.simple.JSONObject;
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


    @PostMapping("/create-account")
    public ResponseEntity<User> createAccount(@RequestBody User userRequest) {
        User user = userService.createAccount(userRequest);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
