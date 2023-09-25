package com.atypon.bootstrap.resourses;

import com.atypon.bootstrap.exceptions.UserException;
import com.atypon.bootstrap.model.Node;
import com.atypon.bootstrap.model.User;
import com.atypon.bootstrap.repositories.UserRepo;
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
    private final UserRepo userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public UserService(List<Node> nodes, UserRepo userRepository, RestTemplate restTemplate) {
        this.nodes = nodes;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }


    public User createAccount(@RequestBody User userRequest) {
        if (userRepository.findById(userRequest.getId()).orElse(null) != null)
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
        jsonObject.put(userRequest.getId(), info);
        restTemplate.postForEntity(minNode.getAddress() + "manage/add-user", jsonObject.toJSONString(), String.class);
        userRepository.save(userRequest);
        minNode.getUsersList().add(userRequest);
        return userRequest;
    }

}
