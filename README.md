# file-way

**File-way** - is a Spring Boot tutorial project that makes it easy and quick for users to share files

---

## Tech stack

| Area                     | Technology                                                   |
|--------------------------|--------------------------------------------------------------|
| Runtime                  | Java **21**                                                  |
| Framework                | Spring Boot **4.x** (Web MVC, Data JPA, Security, Thymeleaf) |
| Database                 | **PostgreSQL**                                               |
| Migrations               | **Flyway**                                                   |
| Build                    | **Maven**                                                    |
| Encryption               | **AES**                                                      |
| Business rule management | **Drools(9.x)**                                              |

---

## Prerequisites

- **JDK 21**
- **PostgreSQL** - can run from **docker-compose.yml** file
- **Encryption key setup (AES)** - The application uses a symmetric **AES-256** key for encryption of sensitive data.
Before running the application, you must generate a key file.

📌 Generate AES key (Linux / macOS / WSL)

From the project root directory run:
```bash
openssl rand -out aes.key 32
```
This generates:
a random **256-bit (32-byte) AES** key
stored in binary format

📌 Create an empty database (name can match your JDBC URL), if you don't use **docker-compose**. On first run, Flyway creates and upgrades the schema from `src/main/resources/db/migration`.

--- 

## Run locally

1. Start PostgreSQL and ensure the database exists.
2. Copy or edit `application.properties` so JDBC settings match your DB, if your run **docker-compose.yml** file, don't edit configuration.
3. Create ```aes.key``` file-key for encryption in project root directory.
4. From the project root:

```bash
./mvnw spring-boot:run
```

If you do not use the Maven wrapper:

```bash
mvn spring-boot:run
```

## Test data
- **Flyway** creates two test users on the first run:
  - `test_1@gmail.com` with password `password123` and role `ROLE_USER`
  - `test_2@gmail.com` with password `password123` and role `ROLE_PREMIUM`

The application listens on **http://localhost:8080/** (unless `server.port` is changed).

