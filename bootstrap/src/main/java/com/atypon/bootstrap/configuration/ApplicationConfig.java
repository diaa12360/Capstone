package com.atypon.bootstrap.configuration;

import com.atypon.bootstrap.model.AuthRequest;
import com.atypon.bootstrap.model.Node;
import com.atypon.bootstrap.model.User;
import com.atypon.bootstrap.repositories.UserRepository;
import com.atypon.bootstrap.resourses.RestTemplateErrorHandler;
import jakarta.annotation.PostConstruct;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Component
@ComponentScan
public class ApplicationConfig {
    @Value("${node.url.first}")
    private String node1URL;
    @Value("${node.url.second}")
    private String node2URL;
    @Value("${node.url.third}")
    private String node3URL;
    private final UserRepository userRepository;
    private List<Node> nodesList;

    @Autowired
    public ApplicationConfig(UserRepository userRepository, List<Node> nodesList) {
        this.userRepository = userRepository;
        this.nodesList = nodesList;
    }

    @PostConstruct
    public void init() {
        RestTemplate restTemplate = restTemplate();
        nodesList = nodes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(nodesList.get(0).getToken());
        HttpEntity<String> entityReq = new HttpEntity<>(jsonForNode1().toJSONString(), headers);
        restTemplate.exchange(node1URL + "manage/init", HttpMethod.POST, entityReq, Object.class);
        headers.setBearerAuth(nodesList.get(1).getToken());
        restTemplate.exchange(node2URL + "manage/init", HttpMethod.POST, new HttpEntity<>(jsonForNode2().toString(), headers), String.class);
        headers.setBearerAuth(nodesList.get(2).getToken());
        restTemplate.exchange(node3URL + "manage/init", HttpMethod.POST, new HttpEntity<>(jsonForNode3().toString(), headers), String.class);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplateBuilder().errorHandler(new RestTemplateErrorHandler()).build();
    }

    private String getTokenForNode(String nodeURL) {
        RestTemplate restTemplate = restTemplate();
        AuthRequest nodeAuthRequest = new AuthRequest("node", "node");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(
                nodeURL.concat("/node/login"),
                nodeAuthRequest,
                String.class,
                headers
        ).getBody();
    }

    @Bean
    public List<Node> nodes() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(
                new Node(
                        node1URL,
                        "node1",
                        userRepository.findAllByNodeAddress(node1URL).orElseThrow(),
                        getTokenForNode(node1URL)
                )
        );
        nodes.add(
                new Node(
                        node1URL,
                        "node2",
                        userRepository.findAllByNodeAddress(node2URL).orElseThrow(),
                        getTokenForNode(node2URL)
                )
        );
        nodes.add(
                new Node(
                        node1URL,
                        "node3",
                        userRepository.findAllByNodeAddress(node3URL).orElseThrow(),
                        getTokenForNode(node3URL)
                )
        );
        return nodes;
    }

    private JSONObject jsonForNode1() {
        JSONObject request = new JSONObject();
        JSONObject temp = new JSONObject();
        JSONObject temp2 = new JSONObject();
        temp2.put("address", node2URL);
        temp2.put("name", "node2");
        temp2.put("affinity", 0L);
        temp2.put("token", nodesList.get(1).getToken());
        temp.put("2", temp2);

        temp2 = new JSONObject();
        temp2.put("address", node3URL);
        temp2.put("name", "node3");
        temp2.put("affinity", 0L);
        temp2.put("token", nodesList.get(2).getToken());


        temp.put("3", temp2);

        request.put("otherNodes", temp);

        List<User> users = userRepository.findAllByNodeAddress(node1URL).orElseThrow();
        temp = new JSONObject();
        for (User user : users) {
            temp2 = new JSONObject();
            temp2.put("password", user.getPassword());
            temp2.put("role", user.getRole());
            temp.put(user.getUsername(), temp2);
        }
        temp2 = new JSONObject();
        temp2.put("password", "node");
        temp2.put("role", "NODE");
        temp.put("node", temp2);
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
        temp2.put("token", nodesList.get(0).getToken());
        temp.put("1", temp2);

        temp2 = new JSONObject();
        temp2.put("address", node3URL);
        temp2.put("name", "node3");
        temp2.put("affinity", 0);
        temp2.put("token", nodesList.get(2).getToken());
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
        temp2 = new JSONObject();
        temp2.put("password", "node");
        temp2.put("role", "NODE");
        temp.put("node", temp2);
        request.put("users", temp);

        temp = new JSONObject();
        temp.put("name", "node2");
        temp.put("address", node2URL);
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
        temp2.put("token", nodesList.get(0).getToken());

        temp.put("1", temp2);

        temp2 = new JSONObject();
        temp2.put("address", node2URL);
        temp2.put("name", "node2");
        temp2.put("affinity", 0);
        temp2.put("token", nodesList.get(1).getToken());

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
        temp2 = new JSONObject();
        temp2.put("password", "node");
        temp2.put("role", "NODE");
        temp.put("node", temp2);
        request.put("users", temp);

        temp = new JSONObject();
        temp.put("address", node3URL);
        temp.put("name", "node3");
        temp.put("affinity", 0L);

        request.put("nodeInfo", temp);
        return request;
    }
}
