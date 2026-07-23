# ETLForge

ETLForge is a Spring Boot application that imports customer data from a CSV file into a relational database using Spring Batch.

The project reads `customers_1000.csv`, maps each row to a `Customer` entity, and saves the data through Spring Data JPA.

## Tech Stack

- Java 21
- Spring Boot
- Spring Batch
- Spring Data JPA
- MySQL
- Maven

## Project Structure

```text
src/main/java/com/nikk
├── config
│   ├── BatchConfig.java
│   └── CustomerProcessor.java
├── controller
│   └── CustomerController.java
├── entity
│   └── Customer.java
├── repo
│   └── ICustomerRepo.java
└── EtlForgeApplication.java

src/main/resources
├── application.properties
└── customers_1000.csv
```

## Features

- Reads customer records from a CSV file
- Uses Spring Batch reader, processor, and writer flow
- Saves customer data into a database
- Provides an HTTP endpoint to start the import job

## CSV Format

The CSV file should contain these columns:

```csv
customerId,firstname,lastname,email,city,state,country,zipcode
```

Example:

```csv
1,Riya,Reddy,riya.reddy1@example.com,Mumbai,MH,India,656538
```

## Database Configuration

Update `src/main/resources/application.properties` with your database details:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/etldb
spring.datasource.username=root
spring.datasource.password=1234

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Create the database before running the application:

```sql
CREATE DATABASE etldb;
```

## How to Run

From the project root, run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

## Import Data

Start the customer import job by opening this endpoint:

```text
GET http://localhost:8080/import
```

Successful response:

```text
Data Loaded
```

## Run Tests

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Notes

- Build output is generated in the `target/` folder and should not be committed.
- The default CSV file is located at `src/main/resources/customers_1000.csv`.
- The batch job currently processes records without changing them in `CustomerProcessor`.
