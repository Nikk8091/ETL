# ETLForge

ETLForge is a configurable, fault-tolerant customer ingestion service built with Java 21, Spring Boot, Spring Batch, Spring Data JPA, and MySQL. It reads CSV data through a chunk-oriented pipeline, validates and normalizes customer records, persists valid data, and reports execution metrics through a REST API.

## Highlights

- Configurable chunk size through `ETL_CHUNK_SIZE` (default: `100`)
- CSV validation and normalization before persistence
- Skip handling for invalid or duplicate records
- Retry handling for transient database lock failures
- REST endpoints to launch imports and inspect execution statistics
- Environment-based database credentials
- H2-backed automated tests and GitHub Actions CI
- Docker Compose setup for local MySQL

## Processing flow

```text
CSV file -> FlatFileItemReader -> CustomerProcessor -> JPA repository -> MySQL
                                     |
                                     `-> validation / normalization / skipped-row metrics
```

The chunk size is runtime-configurable, so the application can be tuned for different file sizes and database capacity without rebuilding it.

## Run locally

Requirements: Java 21 and Docker.

Start MySQL:

```bash
docker compose up -d
```

Set the database credentials used by `compose.yaml` and start the application.

PowerShell:

```powershell
$env:DB_USERNAME="etlforge"
$env:DB_PASSWORD="etlforge"
$env:ETL_CHUNK_SIZE="100"
.\mvnw.cmd spring-boot:run
```

Bash:

```bash
export DB_USERNAME=etlforge
export DB_PASSWORD=etlforge
export ETL_CHUNK_SIZE=100
./mvnw spring-boot:run
```

Additional configuration options are documented in `.env.example`.

## API

Launch an import:

```http
POST /api/v1/imports
```

Example response:

```json
{
  "executionId": 1,
  "status": "COMPLETED",
  "recordsRead": 1000,
  "recordsWritten": 1000,
  "recordsSkipped": 0,
  "startedAt": "2026-08-31T20:15:10",
  "completedAt": "2026-08-31T20:15:11"
}
```

Retrieve the persisted state of an execution:

```http
GET /api/v1/imports/{executionId}
```

## Input format

The configured CSV resource must contain a header followed by these columns:

```csv
customerId,firstname,lastname,email,city,state,country,zipcode
1,Riya,Reddy,riya.reddy1@example.com,Mumbai,MH,India,656538
```

`customerId`, `firstname`, `lastname`, and a valid `email` are required. Text values are trimmed and email addresses are normalized to lowercase.

## Tests

Tests use an isolated in-memory H2 database, so a local MySQL instance is not required:

```bash
./mvnw test
```

The suite covers application startup as well as processor validation and normalization. CI runs the complete Maven verification lifecycle on every push and pull request.

## Tech stack

- Java 21
- Spring Boot 4
- Spring Batch 6
- Spring Data JPA
- MySQL 8
- H2 (tests)
- Maven and GitHub Actions

## Suggested resume description

> Built a configurable, fault-tolerant ETL pipeline with Java, Spring Batch, JPA, and MySQL that validates and imports customer data using chunk-oriented processing, retry/skip policies, REST-based job monitoring, and automated integration tests.
