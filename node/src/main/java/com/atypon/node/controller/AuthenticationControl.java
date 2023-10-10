package com.atypon.node.controller;

import com.atypon.node.jwt.JwtUtils;
import com.atypon.node.dao.UserDao;
import com.atypon.node.model.AuthRequest;
import com.atypon.node.service.Encryption;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@ComponentScan
public class AuthenticationControl {
    private final AuthenticationManager authenticationManager;
    private final UserDao userDao;
    private final JwtUtils jwtUtils;
    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Wrong Password or Username try Again!!", HttpStatus.BAD_REQUEST);
        }
        final UserDetails userDetails = userDao.findUserByUsername(request.getUsername());
        if (userDetails != null) {
            String token = jwtUtils.generateToken(userDetails);
            return new ResponseEntity<>(token, HttpStatus.OK);
        }
        return new ResponseEntity<>("Wrong Password or Username try Again!!", HttpStatus.BAD_REQUEST);
    }

}
