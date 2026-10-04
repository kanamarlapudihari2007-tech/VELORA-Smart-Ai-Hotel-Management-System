# Velora Smart Hotel Management Platform

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Tomcat](https://img.shields.io/badge/Apache_Tomcat-9.0-F8DC75?style=for-the-badge&logo=apache-tomcat&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Google Gemini](https://img.shields.io/badge/Google_Gemini-2.5_Flash-8E75C2?style=for-the-badge&logo=google&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)

**Velora** is an enterprise-grade AI Smart Hotel Management Operating System combining classic 3-tier Java EE architecture with Google Gemini AI intelligence and a precision Vercel-inspired monochromatic design language.

---

## Key Capabilities

- **24/7 AI Guest Concierge**: Multi-turn conversational assistant powered by Google Gemini AI with session memory and live database context.
- **Dynamic Pricing Engine**: Algorithmic yield management recalculating nightly room tariffs from real-time occupancy curves, day-of-week demand, and market intelligence.
- **Autonomous Task Allocation**: Workload-balanced task assignment matrix that dispatches room service orders and guest maintenance issues to specialized departments.
- **AI Service Request Understanding**: Extracts structured item quantities, priority, and fulfillment timing from natural language guest requests.
- **AI Complaint Classification & Escalation**: Classifies issue severity, flags high-urgency maintenance hazards, and notifies management in real time.
- **Verified Review Sentiment Tracking**: Multi-aspect sentiment analysis across cleanliness, staff hospitality, dining, and room amenities with auto-drafted executive replies.
- **Executive Operations Briefing**: Real-time AI generated executive briefings summarizing revenue velocity, room utilization, and risk indicators.
- **Digital Invoicing & Tax Ledger**: Itemized GST-compliant invoices with automated billing and payment audit trails.

---

## Architecture Overview

```
├── Presentation Layer    : JSP, Vanilla CSS (Vercel-inspired design tokens, Geist typography)
├── Controller Layer      : Java Servlets (javax.servlet.*), Role Authentication Filters
├── Service Layer         : GeminiService, DynamicPricingService, TaskAssignmentService, OperationsAnalyticsService
├── Data Access Layer     : DAO Pattern with JDBC PreparedStatement (SQL Injection prevention)
├── Persistence Layer     : MySQL 8.0 Relational Database (InnoDB, Foreign Key Integrity)
└── External AI Services  : Google Gemini API (v1beta REST with HTTP/2 persistent connection pooling)
```

---

## Pre-Configured Test Accounts

| Role | Username | Password | Access Portal |
| :--- | :--- | :--- | :--- |
| **Executive Manager** | `admin` | `admin123` | Manager Executive Console (`/dashboard?role=manager`) |
| **Registered Guest** | `alex_guest` | `guest123` | Guest Services Portal (`/dashboard?role=guest`) |
| **Housekeeping Staff** | `john_housekeeping` | `staff123` | Staff Task Operations (`/dashboard?role=staff`) |
| **Maintenance Staff** | `mike_maintenance` | `staff123` | Staff Task Operations (`/dashboard?role=staff`) |
| **Food & Beverage** | `david_foodservice` | `staff123` | Staff Task Operations (`/dashboard?role=staff`) |
| **Reception / Front Desk** | `sarah_reception` | `staff123` | Staff Task Operations (`/dashboard?role=staff`) |
| **Security Staff** | `robert_security` | `staff123` | Staff Task Operations (`/dashboard?role=staff`) |

---

## Quickstart & Local Installation

### 1. Prerequisites
- **JDK 17** (or JDK 11+)
- **Apache Maven 3.8+**
- **MySQL Server 8.0+**
- **Apache Tomcat 9.0+** (Java EE 8 / `javax.servlet` support)

### 2. Database Setup
1. Start your local MySQL service.
2. Create the database and import the schema:
```bash
mysql -u root -p < schema.sql
```
This creates the `hotel_db` database, tables, foreign keys, and baseline seed data.

### 3. Environment Configuration
Copy `.env.example` to `.env` or configure system environment variables:
```bash
# Database Configuration
export DB_URL="jdbc:mysql://localhost:3306/hotel_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="root"
export DB_PASSWORD="your_password"

# Optional: Google Gemini API Key (for live AI services)
export GEMINI_API_KEY="your_api_key_here"
```
*(Note: If no API key is provided, the application runs graceful rule-based fallbacks).*

### 4. Build and Test
Run the automated test suite and package the production `.war` archive:
```bash
# Run 46 automated unit and integration tests
mvn test

# Package the deployable WAR
mvn clean package -DskipTests
```
The compiled archive is generated at `target/ai-smart-hotel.war`.

### 5. Deployment
- **Standard Tomcat**: Copy `target/ai-smart-hotel.war` to `$CATALINA_HOME/webapps/` and start Tomcat via `bin/startup.sh` (or `bin/startup.bat`).
- **Windows Fast Deployer**: Execute `deploy.bat` or `deploy.ps1` for automated rebuild and Tomcat restart.

Visit **[http://localhost:8080/ai-smart-hotel/](http://localhost:8080/ai-smart-hotel/)** in your browser.

---

## Containerized Deployment (Docker)

Deploy the full stack (MySQL 8 + Apache Tomcat 9) with a single command:

```bash
docker-compose up --build -d
```

- **Web Application**: [http://localhost:8080](http://localhost:8080)
- **MySQL Database**: `localhost:3306` (Database: `hotel_db`, User: `root`, Password: `root`)

To stop:
```bash
docker-compose down
```

---

## Project Structure

```
d:/projects/new2/
├── src/
│   ├── main/
│   │   ├── java/com/hotel/
│   │   │   ├── controller/     # Java Servlets (Login, Booking, Service, Assistant, etc.)
│   │   │   ├── dao/            # Data Access Objects (UserDAO, RoomDAO, TaskDAO, etc.)
│   │   │   ├── filter/         # AuthenticationFilter (Role-based access security)
│   │   │   ├── model/          # Data Models (User, Booking, Room, Task, Review, etc.)
│   │   │   ├── service/        # Business Logic & Gemini AI Integrations
│   │   │   └── util/           # DBConnection, PasswordUtil (BCrypt), DatabaseSeeder
│   │   └── webapp/
│   │       ├── css/            # Vercel-inspired Design System (design-system.css)
│   │       ├── images/         # Velora Logo, Emblem, and 3D Service Artworks
│   │       ├── WEB-INF/        # web.xml Deployment Descriptor
│   │       └── *.jsp           # Front-end Views (index, guest/manager/staff dashboards, etc.)
│   └── test/java/com/hotel/    # JUnit 4 Test Suite (46 Tests across all modules)
├── Dockerfile                  # Multi-stage production container build
├── docker-compose.yml          # Containerized orchestration
├── schema.sql                  # MySQL Relational Database Schema & Seeder
├── pom.xml                     # Maven dependencies & build configuration
├── .gitignore                  # Git repository exclusion rules
└── README.md                   # System documentation
```

---

## Security Features

- **BCrypt Hashing**: Passwords stored using 12-round BCrypt salt hashing (`PasswordUtil.java`).
- **Parameterized SQL**: All DAO database queries use JDBC `PreparedStatement` to prevent SQL Injection.
- **Session Authentication Filter**: Prevents unauthorized URL access, enforces role segregation, and controls session lifetimes.
- **Zero API Key Exposure**: Gemini AI keys resolved through secure server environment variables.

---

## License

This project is licensed under the MIT License.
