package com.eshoplite.user.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name="uk_users_email", columnNames = "email"))
public class User {
  @Id
  @Column(columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, length = 20)
  private String role;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  public void prePersist(){
    this.id = this.id == null ? UUID.randomUUID() : this.id;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
    if (this.role == null) this.role = "USER";
    if (this.status == null) this.status = "ACTIVE";
  }

  @PreUpdate
  public void preUpdate(){ this.updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
