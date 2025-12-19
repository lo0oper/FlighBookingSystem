# FlighBookingSystem
A flight booking system that focuses only on the Booking of flights and preparation of filght schedules. Highlighting the distributed locks importance using aerospike as distributed lock.

## Prerequisites

1.  **Java 17+**
2.  **Maven**
3.  **MariaDB:** A running instance (or use the configured Docker setup).
4.  **Aerospike:** A running instance (or use the configured Docker setup).

## 1. Local Database Setup

### MariaDB

1.  Create a database: `CREATE DATABASE flight_booking_db;`
2.  Update the connection details in `src/main/resources/application.properties`.
    * `spring.datasource.url=jdbc:mariadb://localhost:3306/flight_booking_db`
    * `spring.datasource.username=your_user`
    * `spring.datasource.password=your_password`

### Aerospike

1.  The application expects Aerospike to be running on `localhost:3000`.
2.  Update `application.properties` if needed:
    * `aerospike.host=127.0.0.1`
    * `aerospike.port=3000`

## 2. Build and Run

1.  **Build the project:**
    ```bash
    mvn clean install
    ```
2.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```

The application will start on `http://localhost:8080`.

## 3. API Endpoints

* **POST /api/v1/admin/flights:** Create a new flight (Admin)
* **POST /api/v1/bookings:** Create a booking (User) - Requires `scheduleId`, `userId`, `seatNumber`.
* **GET /api/v1/bookings/{id}:** Retrieve booking details.



## STEPS DONE WHILE BUILDNIG REPO
1. flight/  <-- THIS IS YOUR ROOT DIRECTORY
   ├── pom.xml                                     <-- Maven definition
   ├── Dockerfile                                  <-- Docker build instructions
   ├── docker-compose.yml                          <-- Docker orchestration

└── src/                                        <-- Source Code Root
└── main/
├── java/                               <-- Java Code Root
│   └── com/
│       └── booking/
│           └── flight/
│               ├── FlightBookingSystemApplication.java <-- Main Class
│               ├── config/             <-- DB/Aerospike Config
│               ├── controller/         <-- REST Endpoints (Admin & User)
│               ├── dto/                <-- Request/Response DTOs
│               ├── exception/          <-- Custom Exceptions
│               ├── model/              <-- JPA Entities (Booking, Flight, etc.)
│               ├── repository/         <-- Spring Data JPA Repositories
│               └── service/            <-- Business Logic (BookingService, FlightManagementService)
│
└── resources/                          <-- Configuration Root
└── application.properties          <-- Spring Boot/DB/Aerospike Settings