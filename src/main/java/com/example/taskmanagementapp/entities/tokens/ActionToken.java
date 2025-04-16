package com.example.taskmanagementapp.entities.tokens;

import static com.example.taskmanagementapp.constants.EntitiesConstants.ACTION_TOKENS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = ACTION_TOKENS)
public class ActionToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String actionToken;
}
