package com.example.testApp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Customer {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    // Default constructor required by JPA
    public Customer() {}

    public Customer(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}