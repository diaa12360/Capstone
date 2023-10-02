package com.atypon.node.dao;

import com.atypon.node.model.Document;
import org.json.simple.JSONObject;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Collections;

@Repository
public class UserDao {

    public UserDetails findUserByUsername(String username) {
        Document usersFile = new Document("nodeFiles/users.json");
        usersFile.read();
        JSONObject users = (JSONObject) usersFile.getData().get(username);
        System.out.println(users);
        User user1 = null;
        System.out.println(username + " " + users.get("password"));
        try {
            user1 =
                    new User(
                            username, (String) users.get("password"),
                            Collections.singleton(new SimpleGrantedAuthority((String) users.get("role"))));
        } catch (Exception e) {
            System.out.println("Ex");
        }
        System.out.println("Hello");
        return user1;
    }
}
