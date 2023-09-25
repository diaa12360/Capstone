package com.atypon.bootstrap.repositories;

import com.atypon.bootstrap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryDefinition(domainClass = User.class, idClass = Long.class)
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<List<User>> findAllByNodeAddress(String nodeAddress);
}
