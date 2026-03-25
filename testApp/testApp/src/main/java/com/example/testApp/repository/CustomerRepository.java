package com.example.testApp.repository;

import com.example.testApp.entity.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    // 🛑 DANGER 1: The "Leading Wildcard"
    List<Customer> findByNameContaining(String name);

    // 🛑 DANGER 2: Unindexed Sorting
    List<Customer> findAllByOrderByNameDesc();

    // 🛑 DANGER 3: The "Non-Sargable" Query
    @Query("SELECT c FROM Customer c WHERE UPPER(c.name) = 'JOHN'")
    List<Customer> findWithBadFunction();

    // 🛑 DANGER 4: The Cartesian Product / Cross Join
    @Query(value = "SELECT c1.id, c2.name FROM customer c1, customer c2", nativeQuery = true)
    List<Object[]> triggerCartesianProduct();
}