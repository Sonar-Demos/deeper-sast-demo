package com.dashboardmanager.repository;

import com.dashboardmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, Long> {

    User findById(Integer id);

    User findByName(String name);
}
