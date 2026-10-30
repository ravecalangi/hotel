package com.mycompany.hotelmanagementsystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HotelManager - the "brain" of the system.
 * Holds all rooms and reservations and enforces the booking rules,
 * keeping business logic separate from the GUI (separation of concerns).
 */
public class HotelManager {

    private final List<Room> rooms = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();

    public HotelManager() {
        // POLYMORPHISM: different Room subclasses stored in one List<Room>
        rooms.add(new StandardRoom(101, 1800, 2));
        rooms.add(new StandardRoom(102, 1800, 2));
        rooms.add(new StandardRoom(103, 2000, 3));
        rooms.add(new DeluxeRoom(201, 3200, 2));
        rooms.add(new DeluxeRoom(202, 3200, 3));
        rooms.add(new DeluxeRoom(203, 3600, 4));
        rooms.add(new SuiteRoom(301, 6500, 4));
        rooms.add(new SuiteRoom(302, 8000, 6));
    }

    /** Read-only views so outside classes cannot modify the lists directly. */
    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    /** Checks that no existing reservation overlaps the requested dates. */
    public boolean isAvailable(Room room, LocalDate checkIn, int nights) {
        LocalDate checkOut = checkIn.plusDays(nights);
        for (Reservation r : reservations) {
            if (r.conflictsWith(room, checkIn, checkOut)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates the input and creates a reservation.
     *
     * @throws IllegalArgumentException when an input is invalid
     * @throws IllegalStateException    when the room is already taken
     */
    public Reservation book(String guestName, String contact, Room room,
                            LocalDate checkIn, int nights) {
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter the guest's name.");
        }
        if (contact == null || !contact.trim().matches("[0-9+\\- ]{7,15}")) {
            throw new IllegalArgumentException("Contact number must be 7-15 digits.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (nights < 1 || nights > 30) {
            throw new IllegalArgumentException("Nights must be between 1 and 30.");
        }
        if (!isAvailable(room, checkIn, nights)) {
            throw new IllegalStateException("Room " + room.getRoomNumber()
                    + " is already booked for those dates.");
        }

        Reservation reservation = new Reservation(guestName.trim(), contact.trim(),
                room, checkIn, nights);
        reservations.add(reservation);
        return reservation;
    }

    /** Number of rooms that are free tonight (used by the dashboard). */
    public int countAvailableToday() {
        int free = 0;
        for (Room room : rooms) {
            if (isAvailable(room, LocalDate.now(), 1)) {
                free++;
            }
        }
        return free;
    }

    /** Sum of all active reservation totals (used by the dashboard). */
    public double getTotalRevenue() {
        double sum = 0;
        for (Reservation r : reservations) {
            sum += r.getTotalCost();
        }
        return sum;
    }

    /** Removes a reservation by ID. Returns true if something was cancelled. */
    public boolean cancel(int reservationId) {
        return reservations.removeIf(r -> r.getId() == reservationId);
    }
}