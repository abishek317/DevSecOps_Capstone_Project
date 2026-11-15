# Chapter 5 — Database Design and Configuration

## 5.1 Database Selection

HabitFlow requires persistent relational storage for registered users, tasks, habits, and habit completion history. MySQL is selected as the relational database engine, with Amazon RDS for MySQL planned as the AWS-hosted database service. RDS keeps database administration separate from the application EC2 instance and supports deployment within private subnets.

H2 is retained only as a local in-memory database for quick application smoke tests. H2 data is ephemeral and is cleared when the backend process restarts. It is not the persistent project database.

## 5.2 Database Architecture and Application Connectivity

The Spring Boot backend accesses the database through Spring Data JPA. Entity classes represent application records, repositories provide data access, and Flyway migration scripts create and version the schema. In the AWS architecture, the backend EC2 instance connects to private RDS MySQL on TCP port 3306. The RDS security group should allow this port only from the backend EC2 security group; the database is not publicly accessible.

MySQL Workbench is a database client used to connect, inspect schemas and records, and execute SQL queries. It is not the database server itself. For a private RDS instance, Workbench from a developer laptop requires a secure connectivity path (for example, an SSM port-forwarding tunnel through an authorized instance); it should not be made public solely for convenience.

## 5.3 Relational Schema

Flyway migration `V1__create_core_tables.sql` defines the following tables.

| Table | Purpose | Important fields and relationships |
|---|---|---|
| `app_users` | Stores user account data | `id` primary key; unique `email`; `display_name`; `password_hash`; `created_at` |
| `planner_tasks` | Stores a user's tasks | `id` primary key; `user_id` foreign key to `app_users`; title, description, due date, completion status and timestamps |
| `habits` | Stores habits owned by users | `id` primary key; `user_id` foreign key to `app_users`; name, description and creation timestamp |
| `habit_completions` | Stores daily habit completion history | `id` primary key; `habit_id` foreign key to `habits`; `user_id` foreign key to `app_users`; `completion_date` |

The `app_users` table is the parent of `planner_tasks` and `habits`. A habit has multiple completion records over time. The unique constraint on (`habit_id`, `completion_date`) prevents the same habit from being recorded as completed more than once on the same date. Foreign keys use cascading deletion so that deleting a user or habit removes associated dependent records.

### Entity relationship overview

```text
app_users (1) ───── (many) planner_tasks
    │
    └────────────── (many) habits (1) ───── (many) habit_completions
```

The `habit_completions` records preserve the dates used to calculate current and longest streaks. Streak values are derived from consecutive completion dates rather than being the sole source of truth.

## 5.4 Schema Initialization with Flyway

Flyway is used for version-controlled database schema migrations. The initial migration is stored at:

`backend/src/main/resources/db/migration/V1__create_core_tables.sql`

When the Spring Boot backend starts with Flyway enabled, it checks the migration history and applies any pending migration scripts. The application uses JPA schema validation (`spring.jpa.hibernate.ddl-auto=validate`), so Hibernate validates the schema against the entity definitions rather than automatically creating the tables.

Consequently, tables do not need to be created manually in MySQL Workbench. Flyway should create them during the first backend startup after the database connection is configured.

## 5.5 Local H2 and Persistent MySQL Configuration

The default `application.properties` uses H2 in-memory mode for local smoke testing. It provides a quick path to validate application endpoints without provisioning a database service, but its records are temporary.

The `application-mysql.properties` file provides the MySQL profile. The connection URL, username, and password are supplied using environment variables (`DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`) rather than hardcoded credentials. The MySQL profile is intended to be used after the RDS instance, database, user, network path, and security group rules have been configured.

## 5.6 MySQL Workbench Usage

MySQL Workbench is used after connectivity to the MySQL instance is established. Typical verification activities include:

- Confirming that the `plannerdb` schema exists.
- Inspecting the four application tables.
- Reviewing primary keys, foreign keys, and unique constraints.
- Querying users, tasks, habits, and completion records created through the application.
- Confirming that records remain after the Spring Boot service is restarted.

The exact Workbench connection steps depend on the access method configured for the private RDS instance and will be documented during the infrastructure implementation.

## 5.7 Persistence Verification Plan

After the backend is connected to RDS MySQL:

1. Register a test account through HabitFlow.
2. Create a task and a habit.
3. Complete the habit for a date and verify its completion record.
4. Inspect the corresponding rows using MySQL Workbench.
5. Restart the backend application.
6. Log in again and verify that the task and habit still appear.

Successful completion confirms that application data is stored in persistent MySQL storage rather than the local in-memory H2 database.

## 5.8 Database Setup Work to Be Completed in AWS

The practical database configuration will be completed alongside the VPC implementation:

1. Create private database subnets in two Availability Zones.
2. Create an RDS DB Subnet Group from those subnets.
3. Create a Single-AZ RDS for MySQL instance with public accessibility disabled.
4. Configure a database security group that permits TCP 3306 only from the backend EC2 security group.
5. Create the `plannerdb` database and a dedicated application database user with limited privileges.
6. Establish a secure route for administrative Workbench access.
7. Configure the backend's MySQL profile with the RDS endpoint and credentials from secure configuration.
8. Start the backend and verify Flyway migrations and application persistence.

RDS instance class, storage, backups, and related options must be checked against current account Free Tier/credit eligibility before creation to avoid unexpected charges.
