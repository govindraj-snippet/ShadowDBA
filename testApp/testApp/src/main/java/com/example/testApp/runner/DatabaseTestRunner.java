package com.example.testApp.runner;

import com.example.testApp.entity.Customer;
import com.example.testApp.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTestRunner implements CommandLineRunner {

    private final CustomerRepository repo;

    // Inject the repository cleanly via constructor
    public DatabaseTestRunner(CustomerRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n--- POPULATING DUMMY DATA ---");
        repo.save(new Customer("Alice"));
        repo.save(new Customer("Bob"));
        repo.save(new Customer("Charlie"));

        System.out.println("\n--- FIRING DANGER QUERIES ---");

        repo.findByNameContaining("oli");
        repo.findAllByOrderByNameDesc();
        repo.findWithBadFunction();
        repo.triggerCartesianProduct();

        System.out.println("\n--- TESTS COMPLETE! CHECK YOUR MARKDOWN FILE ---");
    }
}