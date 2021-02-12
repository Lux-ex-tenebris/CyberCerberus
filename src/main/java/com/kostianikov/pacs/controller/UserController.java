package com.kostianikov.pacs.controller;

import com.kostianikov.pacs.model.access.User;
import com.kostianikov.pacs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }



    //@PreAuthorize("hasAuthoriti('read:all')")
    @GetMapping("/users")
    public String findAll(Model model){
        List<User> users = userService.findAll();
        model.addAttribute("users",users);
        return "user-list";
    }

    //@PreAuthorize("hasAuthoriti('read:self')")
    @GetMapping("/user")
    public String getUserpage(HttpServletRequest request, Model model, Principal principal){
        model.addAttribute("name", principal.getName());
        return "user-list";
    }


}
