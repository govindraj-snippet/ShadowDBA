# shadowDBA Performance Reports


## 🚨 Slow Query Alert: 13ms
**Time:** 2026-03-25 17:33:48

### 🛡️ Sanitized SQL:
```sql
select c1_0.id,c1_0.name from customer c1_0 order by c1_0.name desc
```

### 💡 Root Cause
The database lacks an index on the `name` column, forcing a full table scan and in-memory sort for the `ORDER BY` clause, causing performance degradation.

### 🎯 The Minimal Fix
**Target File:** Customer.java

```text
@Column(name = "name")
@org.hibernate.annotations.Index(name = "idx_customer_name", columnNames = {"name"})
private String name;
```

---

## 🚨 Slow Query Alert: 231ms
**Time:** 2026-03-25 17:33:49

### 🛡️ Sanitized SQL:
```sql
select c1_0.id,c1_0.name from customer c1_0 where c1_0.name like ? escape '?'
```

### 💡 Root Cause
The `LIKE` predicate on the `name` column likely causes a full table scan. The database lacks an index to efficiently locate customer records based on their name, leading to slow query execution.

### 🎯 The Minimal Fix
**Target File:** Customer.java

```java
@Table(name = "customer", indexes = {
    @Index(name = "idx_customer_name", columnList = "name")
})
```

---

## 🚨 Slow Query Alert: 108ms
**Time:** 2026-03-25 17:33:59

### 🛡️ Sanitized SQL:
```sql
SELECT c1.id, c2.name FROM customer c1, customer c2
```

### 💡 Root Cause
The database likely performs a full table scan to retrieve all `name` values from the `customer` table for the `c2` alias, as the `name` column might not be indexed. This makes the cross join operation more expensive than necessary.

### 🎯 The Minimal Fix
**Target File:** `Customer.java`

```java
@Column(name = "name", length = 255) // Assuming typical column definition
@Index(name = "idx_customer_name")
private String name;
```

---

## 🚨 Slow Query Alert: 17ms
**Time:** 2026-03-25 17:34:12

### 🛡️ Sanitized SQL:
```sql
select c1_0.id,c1_0.name from customer c1_0 where upper(c1_0.name)='?'
```

### 💡 Root Cause
The `UPPER()` function applied to the `c1_0.name` column in the `WHERE` clause prevents any standard index on `name` from being utilized, forcing a full table scan, making the query inefficient.

### 🎯 The Minimal Fix
**Target File:** Customer.java, CustomerRepository.java

```java
// In Customer.java (Entity)
    @Column(name = "lower_name", nullable = false, length = 255) // Ensure length matches 'name' column
    @Index(name = "idx_customer_lower_name", columnList = "lower_name")
    private String lowerName;

    // Lifecycle methods to automatically maintain the 'lower_name' column
    @PrePersist
    @PreUpdate
    private void updateLowerName() {
        if (this.name != null) {
            this.lowerName = this.name.toLowerCase();
        } else {
            this.lowerName = null; // Or handle null names as per business logic
        }
    }

// In CustomerRepository.java (Spring Data JPA Repository)
    // Use the new indexable 'lowerName' column for case-insensitive searches
    @Query("SELECT c FROM Customer c WHERE c.lowerName = LOWER(:customerName)")
    Optional<Customer> findByNameCaseInsensitive(@Param("customerName") String customerName);
```

---

## 🚨 Slow Query Alert: 99ms
**Time:** 2026-03-25 17:34:19

### 🛡️ Sanitized SQL:
```sql
insert into customer (name,id) values (?,?)
```

### 💡 Root Cause
Frequent database calls to retrieve individual primary key values from the sequence generator before each insert, causing significant latency and round-trip overhead.

### 🎯 The Minimal Fix
**Target File:** Customer.java

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq")
@SequenceGenerator(name = "customer_seq", sequenceName = "customer_seq", allocationSize = 100)
private Long id;
```

---
