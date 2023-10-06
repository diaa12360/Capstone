package com.atypon.bootstrap.repositories;

import com.atypon.bootstrap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryDefinition(domainClass = User.class, idClass = String.class)
public interface UserRepository extends JpaRepository<User, String> {
    Optional<List<User>> findAllByNodeAddress(String nodeAddress);
}
