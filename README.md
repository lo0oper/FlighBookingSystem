# FlighBookingSystem
A flight booking system that focuses only on the Booking of flights and preparation of filght schedules. Highlighting the distributed locks importance using aerospike as distributed lock.

## Prerequisites

1.  **Java 17+**
2.  **Maven**
3.  **MariaDB:** A running instance (or use the configured Docker setup).
4.  **Aerospike:** A running instance (or use the configured Docker setup).

##  Docker  Setup
You can use Docker Compose to set up MariaDB and Aerospike quickly.
1. Docker compose file already exists in the root directory named `docker-compose.yml`.
2. To start the services, run:
    ```bash
    docker-compose up -d
    ```
   3. This will start MariaDB on port `3306` and Aerospike on port `3000`.      
   4. Default MariaDB credentials:
      * Username: `root`
      * Password: `12341234`
      * Database: `flight_booking_db`
      * You can modify these settings in the `docker-compose.yml` file if needed.
      * Make sure to update the `application.properties` file with the correct credentials if you change them.
      * Aerospike runs with default settings and does not require authentication by default.
      * You can access the Aerospike server at `localhost:3000`.
      * Ensure both services are running before starting the application.
      * To stop the services, run:
    ```bash
    docker-compose down
    ```
   

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



## POSTMAN COLLECTION
```json

{
	"info": {
		"_postman_id": "1e7c5b96-12a8-48b4-a21c-a90f23e07c8c",
		"name": "Flight Booking System - End-to-End Test",
		"description": "Full workflow testing Admin setup, Schedule matching, and User Booking concurrency.",
		"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
		"_collection_id": "1e7c5b96-12a8-48b4-a21c-a90f23e07c8c"
	},
	"item": [
		{
			"name": "01. Admin: Create Plane (Boeing 737)",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\n\nconst responseJson = pm.response.json();\n\n// Save the ID for subsequent requests\npm.collectionVariables.set(\"PLANE_ID_737\", responseJson.planeId);\nconsole.log(\"Saved PLANE_ID_737: \" + responseJson.planeId);"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "POST",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": "{\n    \"model\": \"Boeing 737\",\n    \"totalSeats\": 180\n}",
					"options": {
						"raw": {
							"language": "json"
						}
					}
				},
				"url": {
					"raw": "{{BASE_URL}}/admin/management/planes",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"admin",
						"management",
						"planes"
					]
				}
			},
			"response": []
		},
		{
			"name": "02. Admin: Create Plane (Airbus A380)",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\n\nconst responseJson = pm.response.json();\n\n// Save the ID for subsequent requests\npm.collectionVariables.set(\"PLANE_ID_A380\", responseJson.planeId);\nconsole.log(\"Saved PLANE_ID_A380: \" + responseJson.planeId);"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "POST",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": "{\n    \"model\": \"Airbus A380\",\n    \"totalSeats\": 550\n}",
					"options": {
						"raw": {
							"language": "json"
						}
					}
				},
				"url": {
					"raw": "{{BASE_URL}}/admin/management/planes",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"admin",
						"management",
						"planes"
					]
				}
			},
			"response": []
		},
		{
			"name": "03. Admin: Create Flight Route (AI101)",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\n\nconst responseJson = pm.response.json();\n\n// Save the ID for subsequent requests\npm.collectionVariables.set(\"FLIGHT_ID_AI101\", responseJson.flightId);\nconsole.log(\"Saved FLIGHT_ID_AI101: \" + responseJson.flightId);"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "POST",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": "{\n    \"flightNumber\": \"AI101\",\n    \"departureAirport\": \"DEL\",\n    \"arrivalAirport\": \"BOM\",\n    \"planeId\": {{PLANE_ID_737}} \n}",
					"options": {
						"raw": {
							"language": "json"
						}
					}
				},
				"url": {
					"raw": "{{BASE_URL}}/admin/management/flights",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"admin",
						"management",
						"flights"
					]
				}
			},
			"response": []
		},
		{
			"name": "04. Admin: Create Schedule Instance",
			"event": [
				{
					"listen": "prerequest",
					"script": {
						"exec": [
							"// Calculate departure time for tomorrow at 10:00 AM\nconst tomorrow = new Date();\ntomorrow.setDate(tomorrow.getDate() + 1);\ntomorrow.setHours(10, 0, 0, 0);\nconst departureTime = tomorrow.toISOString().replace(/\\.\\d{3}Z$/, '');\n\n// Calculate arrival time for tomorrow at 14:00 PM\ntomorrow.setHours(14, 0, 0, 0);\nconst arrivalTime = tomorrow.toISOString().replace(/\\.\\d{3}Z$/, '');\n\n// Set variables for use in the body\npm.collectionVariables.set(\"departureTime\", departureTime);\npm.collectionVariables.set(\"arrivalTime\", arrivalTime);\n\nconsole.log(\"Departure Time: \" + departureTime);"
						],
						"type": "text/javascript"
					}
				},
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\n\nconst responseJson = pm.response.json();\n\n// Save the ID for booking\npm.collectionVariables.set(\"SCHEDULE_ID\", responseJson.scheduleId);\nconsole.log(\"Saved SCHEDULE_ID: \" + responseJson.scheduleId);"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "POST",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": "{\n    \"flightId\": {{FLIGHT_ID_AI101}},\n    \"departureTime\": \"{{departureTime}}\",\n    \"arrivalTime\": \"{{arrivalTime}}\",\n    \"basePrice\": 5500.00\n}",
					"options": {
						"raw": {
							"language": "json"
						}
					}
				},
				"url": {
					"raw": "{{BASE_URL}}/admin/management/schedules",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"admin",
						"management",
						"schedules"
					]
				}
			},
			"response": []
		},
		{
			"name": "05. Admin: Reassign Plane (Match AI101 to A380)",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 200 OK\", function () {\n    pm.response.to.have.status(200);\n});\n\npm.test(\"Plane ID is updated to A380 ID\", function () {\n    pm.expect(pm.response.json().plane.planeId).to.equal(pm.collectionVariables.get(\"PLANE_ID_A380\"));\n});\n"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "PUT",
				"header": [],
				"url": {
					"raw": "{{BASE_URL}}/admin/management/flights/{{FLIGHT_ID_AI101}}/plane/{{PLANE_ID_A380}}",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"admin",
						"management",
						"flights",
						"{{FLIGHT_ID_AI101}}",
						"plane",
						"{{PLANE_ID_A380}}"
					]
				}
			},
			"response": []
		},
		{
			"name": "06. User: Book Seat A01 (Alice - SUCCESS)",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\n\nconst responseJson = pm.response.json();\npm.collectionVariables.set(\"BOOKING_ID_ALICE\", responseJson.bookingId);\nconsole.log(\"Alice's Booking ID: \" + responseJson.bookingId);"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "POST",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": "{\n    \"scheduleId\": {{SCHEDULE_ID}},\n    \"seatNumber\": \"A01\",\n    \"customerName\": \"Alice Smith\"\n}",
					"options": {
						"raw": {
							"language": "json"
						}
					}
				},
				"url": {
					"raw": "{{BASE_URL}}/booking",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"booking"
					]
				}
			},
			"response": []
		},
		{
			"name": "07. User: Book Seat A01 (Bob - CONFLICT)",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 409 Conflict (Concurrency Test Pass)\", function () {\n    pm.response.to.have.status(409);\n});"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "POST",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": "{\n    \"scheduleId\": {{SCHEDULE_ID}},\n    \"seatNumber\": \"A01\",\n    \"customerName\": \"Bob Johnson\"\n}",
					"options": {
						"raw": {
							"language": "json"
						}
					}
				},
				"url": {
					"raw": "{{BASE_URL}}/booking",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"booking"
					]
				}
			},
			"response": []
		},
		{
			"name": "08. User: Book Seat B02 (Charlie - SUCCESS)",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "POST",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": "{\n    \"scheduleId\": {{SCHEDULE_ID}},\n    \"seatNumber\": \"B02\",\n    \"customerName\": \"Charlie Brown\"\n}",
					"options": {
						"raw": {
							"language": "json"
						}
					}
				},
				"url": {
					"raw": "{{BASE_URL}}/booking",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"booking"
					]
				}
			},
			"response": []
		},
		{
			"name": "09. Confirmation: Check Reserved Seats",
			"event": [
				{
					"listen": "test",
					"script": {
						"exec": [
							"pm.test(\"Status code is 200 OK\", function () {\n    pm.response.to.have.status(200);\n});\n\npm.test(\"Both booked seats are present\", function () {\n    const reservedSeats = pm.response.json();\n    pm.expect(reservedSeats).to.be.an('array').with.lengthOf(2);\n    pm.expect(reservedSeats).to.deep.include({\"seatNumber\": \"A01\", \"customerName\": \"Alice Smith\"});\n    pm.expect(reservedSeats).to.deep.include({\"seatNumber\": \"B02\", \"customerName\": \"Charlie Brown\"});\n});"
						],
						"type": "text/javascript"
					}
				}
			],
			"request": {
				"method": "GET",
				"header": [],
				"url": {
					"raw": "{{BASE_URL}}/booking/schedule/{{SCHEDULE_ID}}/reserved",
					"host": [
						"{{BASE_URL}}"
					],
					"path": [
						"booking",
						"schedule",
						"{{SCHEDULE_ID}}",
						"reserved"
					]
				}
			},
			"response": []
		}
	],
	"variable": [
		{
			"key": "BASE_URL",
			"value": "http://localhost:8080/api/v1"
		},
		{
			"key": "PLANE_ID_737",
			"value": "1"
		},
		{
			"key": "PLANE_ID_A380",
			"value": "2"
		},
		{
			"key": "FLIGHT_ID_AI101",
			"value": "1"
		},
		{
			"key": "SCHEDULE_ID",
			"value": "1"
		},
		{
			"key": "departureTime",
			"value": "2025-12-20T10:00:00"
		},
		{
			"key": "arrivalTime",
			"value": "2025-12-20T14:00:00"
		},
		{
			"key": "BOOKING_ID_ALICE",
			"value": ""
		}
	]
}
```