package com.example.testApp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

// 1. A dummy table in the database
@Entity
class Customer {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
}

// 2. A dummy repository to interact with the table
interface CustomerRepository extends CrudRepository<Customer, Long> {}

@SpringBootApplication
public class TestAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestAppApplication.class, args);
	}

	// 3. This runs automatically right after Spring Boot starts
	@Bean
	CommandLineRunner runTest(CustomerRepository repository) {
		return args -> {
			System.out.println("\n--- DUMMY APP STARTED ---");
			System.out.println("Running a database query. shadowDBA should catch this...\n");

			// This method triggers a "SELECT COUNT(*)" query in the database.
			// Because our threshold is 0ms, shadowDBA will instantly flag it!
			repository.count();
		};
	}
}