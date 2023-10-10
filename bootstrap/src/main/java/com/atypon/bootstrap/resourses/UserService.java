package com.atypon.bootstrap.resourses;

import com.atypon.bootstrap.exceptions.UserException;
import com.atypon.bootstrap.model.Node;
import com.atypon.bootstrap.model.User;
import com.atypon.bootstrap.repositories.UserRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Component
public class UserService {
    private final List<Node> nodes;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public UserService(List<Node> nodes, UserRepository userRepository, RestTemplate restTemplate) {
        this.nodes = nodes;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }


    public User createAccount(User userRequest) {
        System.out.println(userRequest);
        if (userRepository.findById(userRequest.getUsername()).orElse(null) != null)
            throw new UserException("User is already Exist!");
        Node minNode = nodes.get(0);
        for (Node n : nodes) {
            if (minNode.getUsersList().size() > n.getUsersList().size()) {
                minNode = n;
            }
        }
        minNode.getUsersList().add(userRequest);
        userRequest.setNodeAddress(minNode.getAddress());
        JSONObject jsonObject = new JSONObject();
        JSONObject info = new JSONObject();
        info.put("password", userRequest.getPassword());
//        jsonObject.put(userRequest.getUsername(), info);
        jsonObject.put("username", userRequest.getUsername());
        jsonObject.put("password", userRequest.getPassword());
        jsonObject.put("role", userRequest.getRole());
        jsonObject.put("nodeAddress", userRequest.getNodeAddress());
        System.out.println(jsonObject);
        System.out.println(userRequest);
        User user = restTemplate.postForEntity(
                minNode.getAddress() + "manage/add-user", jsonObject, User.class
        ).getBody();
        assert user != null;
        userRepository.save(user);
        minNode.getUsersList().add(user);
        return user;
    }

    public String getNodeUrl(String username) {
        User user = userRepository.findById(username).orElseThrow();
        return user.getNodeAddress();
    }
}
