package com.eshoplite.catalog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(name="uk_products_sku", columnNames = "sku"))
public class Product {
  @Id @Column(columnDefinition = "UUID")
  private UUID id;
  @Column(nullable=false) private String name;
  @Column(nullable=false, length=64) private String sku;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal price;
  @Column(nullable=false, length=3) private String currency = "INR";
  @Column(columnDefinition="TEXT") private String description;
  private String category;
  @Column(columnDefinition="jsonb") private String attributes;
  @Column(nullable=false) private Instant createdAt;
  @Column(nullable=false) private Instant updatedAt;

  @PrePersist public void prePersist(){
    this.id = this.id==null?UUID.randomUUID():this.id; this.createdAt=Instant.now(); this.updatedAt=this.createdAt; }
  @PreUpdate public void preUpdate(){ this.updatedAt = Instant.now(); }
  // getters/setters omitted
}
