package com.blpsteam.blpslab1.data.entities.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Boolean approved;

    @Column(nullable = false, name = "seller_id")
    private Long sellerId;

    @Column(nullable = false, name = "average_rating")
    private Double averageRating = 0.0;

    @Column(nullable = false, name = "review_count")
    private Integer reviewCount = 0;

    @Version
    private Long version;
}
