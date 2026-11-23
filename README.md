# Java E-Commerce Console Management System

Console-based e-commerce management system built with **Java 17**, **Maven 3.9+**, **JUnit Jupiter 6.1.3**, and the **Java Collections Framework** (in-memory only).

---

## 1. Project Overview

The **Java E-Commerce Console Management System** (`com.flipkart:java-ecommerce-console`) simulates an end-to-end command-line e-commerce storefront and back-office administration tool. Users interact as **Guests**, registered **Customers**, or **Admins** through a structured text menu backed by layered services, in-memory repositories, domain validation rules, and immutable audit logging.

---

## 2. Prerequisites & Tech Stack

- **JDK:** Java 17 (LTS)
- **Build Tool:** Apache Maven 3.9+ (tested with Maven 3.9.16)
- **Testing & Coverage:** JUnit Jupiter 6.1.3, Maven Surefire 3.5.2, JaCoCo 0.8.14
- **Runtime Storage:** Java Collections Framework (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `LinkedHashMap`) — strictly in-memory

---

## 3. Build, Test & Run Commands

All commands below are verified from a clean repository clone:

```bash
# Clean and compile main sources
mvn -B clean compile

# Run unit tests only (excludes @Tag("integration"))
mvn -B test -DexcludedGroups=integration

# Run integration smoke tests only (@Tag("integration"))
mvn -B test -Dgroups=integration

# Full build: compile, run all tests, generate JaCoCo coverage report, and package JAR
mvn -B clean verify

# Run the console application from compiled classes
java -cp target/classes com.flipkart.app.Main

# Run the packaged executable JAR
java -jar target/java-ecommerce-console-0.1.0-SNAPSHOT.jar