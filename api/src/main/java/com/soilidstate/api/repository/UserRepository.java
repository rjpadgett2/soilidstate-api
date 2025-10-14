package com.soilidstate.api.repository;

import com.soilidstate.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthSubject(String oauthSubject);
    boolean existsByEmail(String email);
    boolean existsByOauthSubject(String oauthSubject);
}
