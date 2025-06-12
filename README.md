Staff Planner
A Spring Boot application for managing checkout staffing in a supermarket environment. This system replaces manual Excel-based planning with an automated solution that handles employee availability and shift scheduling.
Overview
The Staff Planner application addresses the challenge of checkout staffing deployment in supermarkets. Previously, staffing decisions were made manually using Excel sheets with gut-feeling approaches, while employee availability was tracked in a separate wish book system. This application consolidates these processes into a unified, automated solution.
Features

Employee Management: Automatically creates employee records when needed
Wish Book System: Employees can register their availability and shift preferences
Schedule Planning: Creates optimized schedules based on employee wishes and business requirements
Schedule Viewing: Displays daily schedules with shift assignments
Two-Shift System: Supports early shift (07:00-15:30) and late shift (11:30-20:00)

Business Rules

Each shift requires exactly 2 employees
Employees cannot be assigned to multiple shifts on the same day
Planning requires availability from employees for both shift types
Schedules can be regenerated, replacing previous assignments for the same date

API Endpoints
Wish Book Management

POST /api/wishbook/entry - Add employee availability/preference for a specific date and shift

Schedule Planning

POST /api/planning/create - Generate a schedule based on wish book entries

Schedule Viewing

GET /api/schedule/{date} - View the schedule for a specific date

Technology Stack

Framework: Spring Boot 3.5.0
Java Version: 24
Database: H2 (in-memory for development)
ORM: Spring Data JPA with Hibernate
Testing: JUnit 5, Mockito, Spring Boot Test
Build Tool: Maven
Documentation: OpenAPI/Swagger annotations

Project Structure
src/
├── main/java/com/prototype/staffplanner/
│   ├── controller/          # REST API controllers
│   ├── dto/                 # Data Transfer Objects
│   ├── enums/              # Enumerations (ShiftType)
│   ├── model/              # JPA entities
│   ├── repository/         # Data access layer
│   ├── service/            # Business logic layer
│   └── StaffPlannerApplication.java
├── main/resources/
│   ├── application.yml     # Main configuration
│   └── application-test.yml # Test configuration
└── test/java/              # Test classes
    ├── controller/         # Controller tests
    ├── service/           # Service tests
    ├── integration/       # Integration tests
    └── config/           # Test configuration
Getting Started
Prerequisites

Java 24 or higher
Maven 3.6+ (or use the included Maven wrapper)

Running the Application

Clone the repository
bashgit clone <repository-url>
cd staff-planner

Run with Maven wrapper
bash./mvnw spring-boot:run
Or on Windows:
cmdmvnw.cmd spring-boot:run

Access the application

API Base URL: http://localhost:8080
H2 Console: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: password





Running Tests
bash# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=PlanningServiceTest

# Run integration tests
./mvnw test -Dtest=StaffPlannerIntegrationTest
Usage Examples
1. Add Employee Availability
bash# John Doe wants to work early shift on June 15th
curl -X POST http://localhost:8080/api/wishbook/entry \
  -H "Content-Type: application/json" \
  -d '{
    "employeeName": "John Doe",
    "date": "2025-06-15",
    "shiftType": "EARLY_SHIFT"
  }'
2. Create a Schedule Plan
bash# Create schedule using wish book entry IDs
curl -X POST http://localhost:8080/api/planning/create \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-06-15",
    "wishBookEntryIds": [1, 2, 3, 4]
  }'
3. View Schedule
bash# Get schedule for June 15th
curl http://localhost:8080/api/schedule/2025-06-15
Data Models
Employee

ID (auto-generated)
Name (unique)
Admin flag (default: false)

WishBookEntry

Employee reference
Date
Shift type (EARLY_SHIFT or LATE_SHIFT)
Unique constraint: one entry per employee/date/shift combination

ScheduleEntry

Employee reference
Date
Shift type
Unique constraint: one entry per employee/date

Validation Rules

Wish Book Entry: Employee name, date, and shift type are required
Planning Request: Date and non-empty list of wish book entry IDs required
Schedule Creation: Exactly 2 employees required per shift type
Employee Assignment: No employee can work multiple shifts on the same day

Error Handling
The application includes comprehensive error handling:

400 Bad Request: Validation failures, business rule violations
409 Conflict: Data integrity violations (duplicate entries)
500 Internal Server Error: Unexpected system errors

All errors return structured problem details with timestamps and descriptive messages.
Development
Database Schema
The application uses H2 in-memory database with auto-DDL generation. Schema is created automatically on startup.
Testing Strategy

Unit Tests: Service layer business logic
Controller Tests: API endpoint validation and error handling
Integration Tests: Full workflow testing with real database
Test Configuration: Separate profile with optimized settings
