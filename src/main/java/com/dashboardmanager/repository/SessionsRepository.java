package com.dashboardmanager.repository;

import com.dashboardmanager.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionsRepository extends JpaRepository<Session, Long> {

    Session findBySessionId(String sessionId);

}