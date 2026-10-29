package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserStatus;
import org.hibernate.annotations.Audited;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = Integer.MAX_VALUE)
    private String username;

    @Column(name = "password_hash", nullable = false, length = Integer.MAX_VALUE)
    private String passwordHash;

    @ColumnDefault("'Offline'")
    @Column(name = "status", columnDefinition = "user_status not null")
    private UserStatus status;

    @Column(name = "cookie_hash", length = Integer.MAX_VALUE)
    private String cookieHash;

    @Column(name = "cookie_expires_at")
    private LocalDateTime cookieExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}