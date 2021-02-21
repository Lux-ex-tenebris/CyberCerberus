package com.kostianikov.pacs.repository;

import com.kostianikov.pacs.model.access.Status;
import com.kostianikov.pacs.model.access.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional
    Optional<User> findByName(String name);

    @Modifying
    @Query("update User u set u.status = :status where u.id = :id")
    void updateStatus(@Param(value = "id") long id, @Param(value = "status") Status status);
}
