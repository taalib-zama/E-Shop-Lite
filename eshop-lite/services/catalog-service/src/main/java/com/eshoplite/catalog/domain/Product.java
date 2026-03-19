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

  public UUID getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getSku() { return sku; }
  public void setSku(String sku) { this.sku = sku; }
  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }
  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getAttributes() { return attributes; }
  public void setAttributes(String attributes) { this.attributes = attributes; }
}
