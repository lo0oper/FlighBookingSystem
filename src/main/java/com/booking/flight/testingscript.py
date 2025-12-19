import requests
import json
from datetime import datetime, timedelta

BASE_URL = "http://localhost:8080/api/v1"
ADMIN_URL = f"{BASE_URL}/admin/management"
USER_URL = f"{BASE_URL}/booking"

HEADERS = {"Content-Type": "application/json"}

# Global IDs to be used across tests
PLANE_ID_737 = None
PLANE_ID_A380 = None
FLIGHT_ID_DEL_BOM = None
SCHEDULE_ID = None
BOOKING_ID = None


def create_plane(model: str, total_seats: int) -> int:
    """Creates a new plane model and returns its ID."""
    print(f"\n--- 1. Creating Plane: {model} with {total_seats} seats ---")
    data = {"model": model, "totalSeats": total_seats}
    response = requests.post(f"{ADMIN_URL}/planes", headers=HEADERS, data=json.dumps(data))

    if response.status_code == 201:
        plane = response.json()
        print(f"SUCCESS: Plane '{plane['model']}' created with ID: {plane['planeId']}")
        return plane['planeId']
    else:
        print(f"FAILURE: Status {response.status_code}, Response: {response.text}")
        return None

def create_flight_route(flight_number: str, dep: str, arr: str, plane_id: int) -> int:
    """Creates a permanent flight route and returns its ID."""
    print(f"\n--- 2. Creating Flight Route: {flight_number} ({dep} -> {arr}) ---")
    data = {
        "flightNumber": flight_number,
        "departureAirport": dep,
        "arrivalAirport": arr,
        "planeId": plane_id
    }
    response = requests.post(f"{ADMIN_URL}/flights", headers=HEADERS, data=json.dumps(data))

    if response.status_code == 201:
        flight = response.json()
        print(f"SUCCESS: Flight Route '{flight['flightNumber']}' created with ID: {flight['flightId']}")
        return flight['flightId']
    else:
        print(f"FAILURE: Status {response.status_code}, Response: {response.text}")
        return None

def create_schedule(flight_id: int, base_price: float) -> int:
    """Creates a specific schedule instance for a flight route."""
    global SCHEDULE_ID
    print(f"\n--- 3. Creating Schedule for Flight ID {flight_id} ---")

    # Set times for tomorrow
    tomorrow = datetime.now() + timedelta(days=1)
    dep_time = tomorrow.replace(hour=10, minute=0, second=0, microsecond=0)
    arr_time = tomorrow.replace(hour=14, minute=0, second=0, microsecond=0)

    data = {
        "flightId": flight_id,
        "departureTime": dep_time.isoformat(),
        "arrivalTime": arr_time.isoformat(),
        "basePrice": base_price
    }
    response = requests.post(f"{ADMIN_URL}/schedules", headers=HEADERS, data=json.dumps(data))

    if response.status_code == 201:
        schedule = response.json()
        SCHEDULE_ID = schedule['scheduleId']
        print(f"SUCCESS: Schedule created with ID: {SCHEDULE_ID}. Departure: {dep_time.strftime('%Y-%m-%d %H:%M')}")
        return SCHEDULE_ID
    else:
        print(f"FAILURE: Status {response.status_code}, Response: {response.text}")
        return None


def reassign_plane(flight_id: int, new_plane_id: int):
    """Admin endpoint to change the plane model for a flight route."""
    print(f"\n--- 4. Reassigning Plane for Flight ID {flight_id} to Plane ID {new_plane_id} ---")
    url = f"{ADMIN_URL}/flights/{flight_id}/plane/{new_plane_id}"
    response = requests.put(url, headers=HEADERS)

    if response.status_code == 200:
        updated_flight = response.json()
        print(f"SUCCESS: Flight Route {updated_flight['flightNumber']} now assigned to Plane ID {updated_flight['plane']['planeId']}")
    else:
        print(f"FAILURE: Status {response.status_code}, Response: {response.text}")


def book_seat(schedule_id: int, seat_number: str, customer_name: str) -> bool:
    """User endpoint to book a seat using the concurrency service."""
    global BOOKING_ID
    print(f"\n--- 5. Attempting to Book Seat {seat_number} for Customer {customer_name} ---")
    data = {
        "scheduleId": schedule_id,
        "seatNumber": seat_number,
        "customerName": customer_name
    }
    response = requests.post(USER_URL, headers=HEADERS, data=json.dumps(data))

    if response.status_code == 201:
        booking = response.json()
        BOOKING_ID = booking['bookingId']
        print(f"SUCCESS: Booking confirmed! ID: {BOOKING_ID}, Seat: {booking['seatNumber']}")
        return True
    elif response.status_code == 409:
        print(f"CONFLICT: Seat {seat_number} is already reserved! Test successful.")
        return False
    else:
        print(f"FAILURE: Status {response.status_code}, Response: {response.text}")
        return False

def check_reserved_seats(schedule_id: int):
    """Admin/User endpoint to view currently reserved seats (for confirmation)."""
    print(f"\n--- 6. Checking Reserved Seats for Schedule ID {schedule_id} ---")
    url = f"{USER_URL}/schedule/{schedule_id}/reserved"
    response = requests.get(url)

    if response.status_code == 200:
        seats = response.json()
        print(f"Reserved Seats: {seats}")
    else:
        print(f"FAILURE: Status {response.status_code}, Response: {response.text}")


def main():
    global PLANE_ID_737, PLANE_ID_A380, FLIGHT_ID_DEL_BOM, SCHEDULE_ID

    # ----------------------------------------------------
    # PHASE 1: SETUP ADMIN DATA
    # ----------------------------------------------------
    PLANE_ID_737 = create_plane("Boeing 737", 180)
    PLANE_ID_A380 = create_plane("Airbus A380", 550)

    if not PLANE_ID_737 or not PLANE_ID_A380: return

    FLIGHT_ID_DEL_BOM = create_flight_route("AI101", "DEL", "BOM", PLANE_ID_737)
    if not FLIGHT_ID_DEL_BOM: return

    SCHEDULE_ID = create_schedule(FLIGHT_ID_DEL_BOM, 5500.00)
    if not SCHEDULE_ID: return

    # ----------------------------------------------------
    # PHASE 2: MATCHING (Admin Functionality)
    # ----------------------------------------------------
    # Reassign the route AI101 to the larger Airbus A380 plane model
    reassign_plane(FLIGHT_ID_DEL_BOM, PLANE_ID_A380)

    # ----------------------------------------------------
    # PHASE 3: BOOKING (User Concurrency Test)
    # ----------------------------------------------------

    # A. Successful booking (Customer Alice)
    book_seat(SCHEDULE_ID, "A01", "Alice Smith")

    # B. Failed booking (Customer Bob tries to book the same seat)
    book_seat(SCHEDULE_ID, "A01", "Bob Johnson") # Should result in 409 CONFLICT

    # C. Another successful booking (Customer Charlie)
    book_seat(SCHEDULE_ID, "B02", "Charlie Brown")

    # ----------------------------------------------------
    # PHASE 4: CONFIRMATION
    # ----------------------------------------------------
    check_reserved_seats(SCHEDULE_ID)


if __name__ == "__main__":
    main()