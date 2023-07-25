package com.dashboardmanager.utils;

import java.io.Serializable;

public class SessionHeader implements Serializable {
    private String username;
    private String sessionId;

    public SessionHeader(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }

    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }

    public String getSessionId() { return this.sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
