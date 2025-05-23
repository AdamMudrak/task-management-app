package com.example.taskmanagementapp.entities;

import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.BOOLEAN_TO_INT;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.FIRST_NAME;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.IS_ACCOUNT_NON_LOCKED;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.IS_ENABLED;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.LAST_NAME;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.ROLE_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.USERS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = USERS)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false, name = FIRST_NAME)
    private String firstName;
    @Column(nullable = false, name = LAST_NAME)
    private String lastName;
    @ManyToOne
    @JoinColumn(name = ROLE_ID)
    private Role role;
    @Column(nullable = false, columnDefinition = BOOLEAN_TO_INT, name = IS_ENABLED)
    private boolean isEnabled;
    @Column(nullable = false, columnDefinition = BOOLEAN_TO_INT, name = IS_ACCOUNT_NON_LOCKED)
    private boolean isAccountNonLocked = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        return roles;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }
}
