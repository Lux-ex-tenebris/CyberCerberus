package com.kostianikov.pacs.service;

import com.kostianikov.pacs.model.access.Status;
import com.kostianikov.pacs.model.access.User;
import com.kostianikov.pacs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {


    @Autowired
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id){
        return userRepository.getOne(id);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public User saveUser(User user){
        return userRepository.save(user);
    }

    public void deleteById(Long id){
        userRepository.deleteById(id);
    }

    public Optional<User> findByName(String name){ return userRepository.findByName(name); }

    public void updateStatusDeleted(long id){
        userRepository.updateStatus(id, Status.DELETED);
    }


}
