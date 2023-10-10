package com.atypon.node.controller;

import com.atypon.node.jwt.JwtUtils;
import com.atypon.node.model.AuthRequest;
import com.atypon.node.model.User;
import com.atypon.node.service.BootstrappingService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping("/manage")
public class ManagementControl {

    @Value("${bootstrap.username}")
    private String username;
    @Value("${bootstrap.password}")
    private String password;
    private final BootstrappingService service;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public ManagementControl(BootstrappingService service, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
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

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException ignore) {
            //ignored
        }
        if (request.getUsername().equals(username) && request.getPassword().equals(password)) {
            final UserDetails userDetails = org.springframework.security.core.userdetails.User.builder().
                    username(username).password(password).roles("NODE").build();
            String token = jwtUtils.generateToken(userDetails);
            return new ResponseEntity<>(token, HttpStatus.OK);
        }
        return new ResponseEntity<>("Wrong Password or Username try Again!!", HttpStatus.BAD_REQUEST);
    }

}
