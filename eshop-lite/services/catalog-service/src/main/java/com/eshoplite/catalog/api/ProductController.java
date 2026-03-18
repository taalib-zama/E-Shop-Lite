package com.eshoplite.catalog.api;

import com.eshoplite.catalog.domain.Product;
import com.eshoplite.catalog.repo.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {
  private final ProductRepository repo;
  public ProductController(ProductRepository repo){ this.repo = repo; }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product create(@Valid @RequestBody CreateProductRequest r){
    if (repo.existsBySku(r.sku())) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Duplicate SKU");
    Product p = new Product();
    p.setName(r.name()); p.setSku(r.sku()); p.setPrice(r.price());
    p.setCurrency(r.currency()!=null?r.currency():"INR");
    p.setDescription(r.description()); p.setCategory(r.category());
    return repo.save(p);
  }

  @GetMapping
  public Page<Product> list(@RequestParam Optional<String> query,
                            @RequestParam Optional<String> category,
                            @RequestParam Optional<BigDecimal> minPrice,
                            @RequestParam Optional<BigDecimal> maxPrice,
                            @RequestParam(defaultValue="0") int page,
                            @RequestParam(defaultValue="20") int size){
    // Simplified: return all paged; you can implement filters later
    return repo.findAll(PageRequest.of(page, size));
  }

  @GetMapping("/{id}")
  public Product get(@PathVariable UUID id){
    return repo.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
