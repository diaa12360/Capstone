package com.atypon.bootstrap;

import com.atypon.bootstrap.model.Node;
import com.atypon.bootstrap.model.User;
import com.atypon.bootstrap.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Component
@ComponentScan
public class Config {
    @Value("${node.url.first}")
    private String node1URL;
    @Value("${node.url.second}")
    private String node2URL;
    @Value("${node.url.third}")
    private String node3URL;
    private final UserRepository userRepository;

    @Autowired
    public Config(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Node node1() {
        Node node = new Node(node1URL, "node1");
        node.setUsersList(userRepository.findAllByNodeAddress(node1URL).orElseThrow());
        return node;
    }

    public Node node2() {
        Node node = new Node(node2URL, "node2");
        node.setUsersList(userRepository.findAllByNodeAddress(node2URL).orElseThrow());
        return node;
    }

    public Node node3() {
        Node node = new Node(node3URL, "node3");
        node.setUsersList(userRepository.findAllByNodeAddress(node3URL).orElseThrow());
        return node;
    }

    @PostConstruct
    public void init() {
//        User admin = new User("admin", "admin password");
//        User normalUser = new User("user", "user password");
//        userRepository.save(admin, normalUser);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(node1URL + "manage/init", new HttpEntity<>(jsonForNode1().toString()), String.class);
//        restTemplate.postForEntity(node2URL + "manage/init", new HttpEntity<>(jsonForNode2().toString()), String.class);
//        restTemplate.postForEntity(node3URL + "manage/init", new HttpEntity<>(jsonForNode3().toString()), String.class);
    }

    @Bean
    public List<Node> nodes() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(node1());
        nodes.add(node2());
        nodes.add(node3());
        return nodes;
    }

    private JSONObject jsonForNode1() {
        JSONObject request = new JSONObject();
        JSONObject temp = new JSONObject();
        JSONObject temp2 = new JSONObject();
//        temp2.put("address", node2URL);
//        temp2.put("name", "node2");
//        temp2.put("affinity", 0L);
//        temp.put("2", temp2);
//
//        temp2 = new JSONObject();
//        temp2.put("address", node3URL);
//        temp2.put("name", "node3");
//        temp2.put("affinity", 0L);
//
//        temp.put("3", temp2);
//
        request.put("otherNodes", temp);

        List<User> users = userRepository.findAllByNodeAddress(node1URL).orElseThrow();
        temp = new JSONObject();
        for (User user : users) {
            temp2 = new JSONObject();
            temp2.put("password", user.getPassword());
            temp2.put("role", user.getRole());
            temp.put(user.getUsername(), temp2);
        }
        request.put("users", temp);

        temp = new JSONObject();
        temp.put("address", node1URL);
        temp.put("name", "node1");
        temp.put("affinity", 0L);

        request.put("nodeInfo", temp);
        return request;
    }

    private JSONObject jsonForNode2() {
        JSONObject request = new JSONObject();
        JSONObject temp = new JSONObject();
        JSONObject temp2 = new JSONObject();
        temp2.put("address", node1URL);
        temp2.put("name", "node1");
        temp2.put("affinity", 0);
        temp.put("1", temp2);

        temp2 = new JSONObject();
        temp2.put("address", node3URL);
        temp2.put("name", "node3");
        temp2.put("affinity", 0);

        temp.put("3", temp2);

        request.put("otherNodes", temp);

        List<User> users = userRepository.findAllByNodeAddress(node2URL).orElseThrow();
        temp = new JSONObject();
        for (User user : users) {
            temp2 = new JSONObject();
            temp2.put("password", user.getPassword());
            temp2.put("role", user.getRole());
            temp.put(user.getUsername(), temp2);
        }
        request.put("users", temp);

        temp = new JSONObject();
        temp.put("name", node2().getName());
        temp.put("address", node2().getAddress());
        temp.put("affinity", 0L);

        request.put("nodeInfo", temp);

        return request;
    }

    private JSONObject jsonForNode3() {
        JSONObject request = new JSONObject();
        JSONObject temp = new JSONObject();
        JSONObject temp2 = new JSONObject();
        temp2.put("address", node1URL);
        temp2.put("name", "node1");
        temp2.put("affinity", 0);

        temp.put("1", temp2);

        temp2 = new JSONObject();
        temp2.put("address", node2URL);
        temp2.put("name", "node2");
        temp2.put("affinity", 0);

        temp.put("2", temp2);
        request.put("otherNodes", temp);

        List<User> users = userRepository.findAllByNodeAddress(node3URL).orElseThrow();
        temp = new JSONObject();
        for (User user : users) {
            temp2 = new JSONObject();
            temp2.put("password", user.getPassword());
            temp2.put("role", user.getRole());
            temp.put(user.getUsername(), temp2);
        }
        request.put("users", temp);

        temp = new JSONObject();
        temp.put("address", node3URL);
        temp.put("name", "node3");
        temp.put("affinity", 0L);

        request.put("nodeInfo", temp);
        return request;
    }
}
