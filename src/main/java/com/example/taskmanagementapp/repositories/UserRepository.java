package com.example.taskmanagementapp.repositories;

import com.example.taskmanagementapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
