package com.dashboardmanager.model;

import jakarta.persistence.*;

@Table(name = "Sessions")
@Entity
public class Session {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @OneToOne
    private User user;

    @Column
    private String sessionId;

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return this.user; }
    public void setUser(User user) { this.user = user; }

    public String getSessionId() { return this.sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

}
