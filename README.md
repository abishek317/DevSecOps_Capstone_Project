# CAP-28 — HabitFlow Task & Habit Planner (Consolidated Version 4)

This is the consolidated application project containing the previously validated frontend/backend fixes:
- Frontend API base URL points to backend port **8081**.
- Task and habit forms preserve their form reference during asynchronous requests.
- Backend CORS configuration permits `PATCH` for task completion.

## Local quick run (H2 in-memory)

Prerequisites: Java 21 JDK, Maven 3.9+, Node.js 20+ (npm included).

### Backend — PowerShell terminal 1

```powershell
cd backend
mvn spring-boot:run
```

Backend base URL: `http://localhost:8081`

Health check from a second terminal:

```powershell
Invoke-RestMethod http://localhost:8081/api/health
```

The default profile uses H2 **in-memory** storage. H2 schema is initialized by Flyway, but records are reset when the backend process is stopped/restarted. This mode is for local smoke testing, not durable storage.

### Frontend — PowerShell terminal 2

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Build validation

Backend:

```powershell
cd backend
mvn test
mvn package
```

Frontend (run from `frontend`):

```powershell
npm run build
```

## Persistent MySQL / RDS profile

The backend has a `mysql` Spring profile (`application-mysql.properties`). Configure the environment variables before running:

- `DB_URL`: JDBC URL, e.g. `jdbc:mysql://<RDS-ENDPOINT>:3306/plannerdb?serverTimezone=UTC`
- `DB_USERNAME`: application database username
- `DB_PASSWORD`: application database password

PowerShell example (use actual values; do not commit credentials):

```powershell
$env:DB_URL='jdbc:mysql://YOUR-RDS-ENDPOINT:3306/plannerdb?serverTimezone=UTC'
$env:DB_USERNAME='planner_app'
$env:DB_PASSWORD='YOUR_PASSWORD'
cd backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=mysql"
```

RDS must first be provisioned, network-reachable from the backend, and have the `plannerdb` database and application user configured. Flyway applies the versioned SQL migration on backend startup. MySQL Workbench is used to inspect/query the database; it does not need to create the application tables manually.

See `docs/Database_Design_and_Configuration.md` for the database chapter draft and schema explanation.

## API overview

- `GET /api/health`
- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`
- `GET/POST /api/tasks`, `PUT/DELETE /api/tasks/{id}`, `PATCH /api/tasks/{id}/complete?completed=true|false`
- `GET/POST /api/habits`, `PUT/DELETE /api/habits/{id}`, `POST /api/habits/{id}/complete[?date=YYYY-MM-DD]`

Task and habit APIs require an authenticated session. This is a compact demonstration application and is not production-hardened.
