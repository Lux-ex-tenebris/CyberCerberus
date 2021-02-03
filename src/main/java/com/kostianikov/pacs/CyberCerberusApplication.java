package com.kostianikov.pacs;

import org.opencv.core.Core;
import org.opencv.face.Face;
import org.opencv.face.Facemark;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.sql.DataSourceDefinition;
import javax.sql.DataSource;

@SpringBootApplication
//@ComponentScan({"com.kostianikov.pacs","com.kostianikov.pacs.controller","com.kostianikov.pacs.repository", "com.kostianikov.pacs.model", "com.kostianikov.pacs.service"})

//@EnableJpaRepositories("com.kostianikov.pacs.repository")
public class CyberCerberusApplication {

//    @Bean
//    public DataSource getDataSourse(){
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.driverClassName("com.mysql.jdbc.Driver");
//        dataSourceBuilder.url("jdbc:mysql://localhost:3306/pacsdata?serverTimezone=UTC ");
//        dataSourceBuilder.username("root");
//        dataSourceBuilder.password("Root");
//        return dataSourceBuilder.build();
//    }

    static {
        try{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }catch (UnsatisfiedLinkError ignore){}
    }
    public static void main(String[] args) {
        SpringApplication.run(CyberCerberusApplication.class, args);
    }



}
