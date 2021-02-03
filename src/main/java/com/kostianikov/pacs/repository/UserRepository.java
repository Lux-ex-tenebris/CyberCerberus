package com.kostianikov.pacs.repository;

import com.kostianikov.pacs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;



public interface UserRepository extends JpaRepository<User, Long> {
}
