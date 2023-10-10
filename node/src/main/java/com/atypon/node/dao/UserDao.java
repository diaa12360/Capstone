package com.atypon.node.dao;

import com.atypon.node.model.MetadataFile;
import org.json.simple.JSONObject;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Objects;

@Repository
public class UserDao {

    public UserDetails findUserByUsername(String username) {
        if (Objects.equals(username, "node"))
            return new User("node", "node", Collections.singleton(new SimpleGrantedAuthority("NODE")));
        MetadataFile usersFile = new MetadataFile("nodeFiles/users.json");
        usersFile.readData();
        JSONObject users = (JSONObject) usersFile.getData().get(username);
        User user1 = null;
        try {
            user1 =
                    new User(
                            username, (String) users.get("password"),
                            Collections.singleton(new SimpleGrantedAuthority((String) users.get("role"))));
        } catch (Exception e) {
            System.out.println("Ex");
        }
        return user1;
    }
}
