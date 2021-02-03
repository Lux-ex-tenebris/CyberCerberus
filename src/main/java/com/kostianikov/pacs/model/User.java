package com.kostianikov.pacs.model;


import javax.persistence.*;

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

    protected User(){

    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", networkid=" + networkid +
                ", pathtophoto='" + pathtophoto + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNetworkid() {
        return networkid;
    }

    public void setNetworkid(Long networkid) {
        this.networkid = networkid;
    }

    public String getPathtophoto() {
        return pathtophoto;
    }

    public void setPathtophoto(String pathtophoto) {
        this.pathtophoto = pathtophoto;
    }
}
