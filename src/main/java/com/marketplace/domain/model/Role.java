package com.marketplace.domain.model;

import java.util.Set;

public class Role {

    private Long id;
    private RoleName name;
    private Set<User> users;

    public enum RoleName {
        ADMIN, USER
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}


