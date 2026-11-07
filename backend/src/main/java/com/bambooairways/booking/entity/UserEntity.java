package com.bambooairways.booking.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "email"),
        @Index(columnList = "username")
})
public class UserEntity {
    @Id public String id;
    @Column(unique = true, nullable = false) public String username;
    @Column(unique = true, nullable = false) public String email;
    @Column(nullable = false) public String passwordHash;
    public String fullName;
    public String identityNumber;
    public String phone;
    public String gender;
    public String memberNumber;
    @Enumerated(EnumType.STRING) public UserRole role = UserRole.CUSTOMER;
    public Instant dateOfBirth;
    public Instant createdAt;
    public Instant updatedAt;

    @PrePersist
    void beforeCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = Instant.now();
    }
}
