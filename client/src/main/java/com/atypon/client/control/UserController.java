package com.atypon.client.control;

import com.atypon.client.exception.DatabaseException;
import com.atypon.client.model.AuthRequest;
import com.atypon.client.model.Collection;
import com.atypon.client.model.Document;
import com.atypon.client.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Component
@ComponentScan
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/get-url")
    public ResponseEntity<String> getUrl(@RequestParam String username) {
        return ResponseEntity.ok(service.nodeUrl(username));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
        String token = service.login(authRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/create-record")
    public ResponseEntity<Document> createDocument(@RequestBody Document document) {
        Document document1 = service.createRecord(document);
        return ResponseEntity.ok(document1);
    }


    @PostMapping("/delete-record")
    public ResponseEntity<?> deleteDocument(@RequestBody Document document) {
        service.deleteRecord(document);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/modify-record")
    public ResponseEntity<?> modifyDocument(@RequestBody Document document) {
        return ResponseEntity.ok(service.modifyRecord(document));
    }

    @PostMapping("/create-database")
    public ResponseEntity<?> createDatabase(@RequestParam String dbName) {
        try {
            service.createDatabase(dbName);
        } catch (DatabaseException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/delete-database")
    public ResponseEntity<Document> deleteDatabase(@RequestParam String dbName) {
        service.deleteDatabase(dbName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/create-collection")
    public ResponseEntity<Collection> createCollection(@RequestBody Collection collection) {
        Collection newCollection = service.createCollection(collection);
        return new ResponseEntity<>(newCollection, HttpStatus.OK);
    }

    @DeleteMapping("/delete-collection")
    public ResponseEntity<?> deleteCollection(@RequestBody Collection collection){
        service.deleteCollection(collection);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/connect-to-database")
    public ResponseEntity<String> connectToDatabase(@RequestParam String dbName) {
        return new ResponseEntity<>(service.connectToDatabase(dbName), HttpStatus.OK);
    }

    @GetMapping("/find")
    public ResponseEntity<?> find(@RequestParam String collectionName,
                                  @RequestParam String prop,
                                  @RequestParam String value) {
        Document data = service.getDataOne(collectionName, prop, value);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/find-all")
    public ResponseEntity<?> findAll(@RequestParam String collectionName,
                                  @RequestParam String prop,
                                  @RequestParam String value) {
        List<Document> data = service.getAll(collectionName, prop, value);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/get-databases")
    public ResponseEntity<List<String>> getDatabases(){
        return ResponseEntity.ok(service.getDatabases());
    }

    @GetMapping("/get-collections")
    public ResponseEntity<List<String>> getCollections() {
        return ResponseEntity.ok(service.getCollections());
    }

    @GetMapping("/get-collection-props")
    public ResponseEntity<String> getProps(String collectionName){
        return ResponseEntity.ok(service.getProps(collectionName));
    }
}
