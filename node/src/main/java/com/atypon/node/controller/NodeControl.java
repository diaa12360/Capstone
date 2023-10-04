package com.atypon.node.controller;

import com.atypon.node.model.Collection;
import com.atypon.node.model.Document;
import com.atypon.node.model.Node;
import com.atypon.node.service.NodeService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/node")
public class NodeControl {
    public final NodeService nodeService;

    @Autowired
    public NodeControl(NodeService nodeService) {
        this.nodeService = nodeService;
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

    @PutMapping("/modify")
    public ResponseEntity<Document> modify(@RequestBody Map<String, Document> documentHashMap){
        return ResponseEntity.ok(
                nodeService.modify(
                        documentHashMap.get("before"),
                        documentHashMap.get("after")
                )
        );
    }

}