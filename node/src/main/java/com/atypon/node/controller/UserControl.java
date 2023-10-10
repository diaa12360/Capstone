package com.atypon.node.controller;

import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserControl {

    private final UserService userService;

    @PostMapping("/create-document")
    public ResponseEntity<Document> createFile(@RequestBody Document document) {
        Document newdocument = userService.createDocument(document);
        return new ResponseEntity<>(newdocument, HttpStatus.CREATED);

    }

    @PostMapping("/create-collection")
    public ResponseEntity<Collection> createCollection(@RequestBody Collection collection) {
        Collection newCollection = userService.createCollection(collection);
        return new ResponseEntity<>(newCollection, HttpStatus.CREATED);
    }

    @PostMapping("/create-database")
    public ResponseEntity<?> createDatabase(@RequestParam String dbName) {
        userService.createDatabase(dbName);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @DeleteMapping("/delete-document")
    public ResponseEntity<?> deleteDocument(@RequestBody Document document) {
        userService.deleteDocument(document);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @DeleteMapping("/delete-collection")
    public ResponseEntity<?> deleteCollection(@RequestBody Collection collection) {
        userService.deleteCollection(collection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-database")
    public ResponseEntity<?> deleteDatabase(@RequestParam String dbName) {
        userService.deleteDatabase(dbName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/find")
    public ResponseEntity<Document> find(@RequestParam String collection,
                                         @RequestParam String property,
                                         @RequestParam String value) {
        Document document = userService.find(collection, property, value);
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @GetMapping("/find-all")
    public ResponseEntity<List<Document>> findAll(@RequestParam String collection,
                                                  @RequestParam String property,
                                                  @RequestParam String value) {
        try {
            List<Document> documents = userService.findAll(collection, property, value);
            return new ResponseEntity<>(documents, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().header("error", e.getMessage()).build();
        }
    }

    @GetMapping("/connect-to-database")
    public ResponseEntity<?> connectToDatabase(@RequestParam String dbName) {
        userService.connectToDatabase(dbName);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("get-databases")
    public ResponseEntity<List<String>> getDatabases() {
        List<String> databases = userService.getDatabases();
        return new ResponseEntity<>(databases, HttpStatus.OK);
    }

    @GetMapping("get-collections")
    public ResponseEntity<List<String>> getCollections() {
        List<String> databases = userService.getCollections();
        return new ResponseEntity<>(databases, HttpStatus.OK);
    }

    @PutMapping("/modify-document")
    public ResponseEntity<Document> modifyDocument(@RequestBody Document after){
        return ResponseEntity.ok(userService.modifyDocument(after));
    }

    @GetMapping("/get-props")
    public ResponseEntity<String> getProps(String collectionName){
        return ResponseEntity.ok(userService.getProps(collectionName));
    }
}
