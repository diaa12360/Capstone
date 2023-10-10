package com.atypon.node.controller;

import com.atypon.node.jwt.JwtUtils;
import com.atypon.node.model.AuthRequest;
import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.model.Node;
import com.atypon.node.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/node")
public class NodeControl {
    private final NodeService nodeService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Value("${node.username}")
    private String username;
    @Value("${node.password}")
    private String password;

    @Autowired
    public NodeControl(NodeService nodeService, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.nodeService = nodeService;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/create-document")
    public ResponseEntity<?> createFile(@RequestBody Document document, @RequestParam(name = "name") String nodeName) {
        nodeService.createFile(document, nodeName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/create-collection")
    public ResponseEntity<?> createCollection(@RequestBody Collection collection) {
        nodeService.createCollection(collection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/create-database")
    public ResponseEntity<?> createDatabase(@RequestParam String dbName) {
        nodeService.createDatabase(dbName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-document")
    public ResponseEntity<?> deleteDocument(@RequestBody Document document) {
        nodeService.deleteDocument(document);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @DeleteMapping("/delete-collection")
    public ResponseEntity<?> deleteCollection(@RequestBody Collection collection) {
        nodeService.deleteCollection(collection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-database")
    public ResponseEntity<?> deleteDatabase(@RequestParam String dbName) {
        nodeService.deleteDatabase(dbName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Node>> getNodes() {
        return new ResponseEntity<>(NodeService.getNodes(), HttpStatus.OK);
    }

    @PostMapping("/decrease-affinity")
    public ResponseEntity<?> decreaseAffinity(@RequestParam String nodeName) {
        nodeService.decreaseAffinity(nodeName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/modify-document")
    public ResponseEntity<Document> modifyDocument(@RequestBody Map<String, Document> documentHashMap) {
        return ResponseEntity.ok(
                nodeService.modifyDocumentForOthers(
                        documentHashMap.get("documentBefore"),
                        documentHashMap.get("documentAfter")
                )
        );
    }

    @PostMapping("/modify")
    public ResponseEntity<Document> modify(@RequestBody Map<String, Document> documentHashMap){
        return ResponseEntity.ok(
                nodeService.modify(
                        documentHashMap.get("before"),
                        documentHashMap.get("after")
                )
        );
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

/*
Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJub2RlIiwiZXhwIjoxNjk2NzI5OTk3LCJpYXQiOjE2OTY2OTM5OTd9.DzBRNj-MEEI2NwfL6UATkOIMV9m0Rff3IMTAeg1Et9Y
 */