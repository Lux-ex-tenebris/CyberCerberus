package com.kostianikov.pacs.model.access;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "networkid")
    private Long networkid;
    @Column(name = "pathtophoto")
    private String pathtophoto;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "password")
    private String password;

    protected User(){

    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", networkid=" + networkid +
                ", pathtophoto='" + pathtophoto + '\'' +
                ", role=" + role +
                ", status='" + status + '\'' +
                '}';
    }

}
