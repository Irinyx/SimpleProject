package org.simple.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import javax.persistence.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Entity
@Table(name = "users")
public class User implements Comparable<User> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "login")
    private String login;

    @Override
    public int compareTo(User user) {
        return 0;
    }
}

