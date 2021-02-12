package com.kostianikov.pacs.repository;

import com.kostianikov.pacs.model.access.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional
    Optional<User> findByName(String name);
}
