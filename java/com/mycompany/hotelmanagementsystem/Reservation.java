package com.mycompany.hotelmanagementsystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Reservation - stores the details of one booking.
 * ENCAPSULATION: private final fields, read-only access through getters.
 */
public class Reservation {

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private static int idCounter = 1000; // auto-incrementing reservation number

    private final int id;
    private final String guestName;
    private final String contact;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int nights;
    private final double totalCost;

    public Reservation(String guestName, String contact, Room room, LocalDate checkIn, int nights) {
        this.id = ++idCounter;
        this.guestName = guestName;
        this.contact = contact;
        this.room = room;
        this.checkIn = checkIn;
        this.nights = nights;
        this.checkOut = checkIn.plusDays(nights);
        this.totalCost = room.calculateTotal(nights); // polymorphic call
    }

    public int getId() {
        return id;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getContact() {
        return contact;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getNights() {
        return nights;
    }

    public double getTotalCost() {
        return totalCost;
    }

    /**
     * True if this reservation occupies the given room during [in, out).
     * The check-out day is free, so back-to-back stays are allowed.
     */
    public boolean conflictsWith(Room other, LocalDate in, LocalDate out) {
        return room.getRoomNumber() == other.getRoomNumber()
                && in.isBefore(checkOut)
                && out.isAfter(checkIn);
    }

    // ---- Display helpers used by the GUI ----

    /** Short reference such as "#1001". */
    public String getReference() {
        return "#" + id;
    }

    /** Stay range such as "Dec 24 - Dec 26, 2026". */
    public String getStayLabel() {
        return checkIn.format(SHORT_DATE) + " - " + checkOut.format(LONG_DATE);
    }

    public String getCheckInLabel() {
        return checkIn.format(LONG_DATE);
    }

    public String getCheckOutLabel() {
        return checkOut.format(LONG_DATE);
    }

    /** "1 night" or "3 nights". */
    public String getNightsLabel() {
        return nights + (nights == 1 ? " night" : " nights");
    }

    @Override
    public String toString() {
        return getReference() + " " + guestName + " - Room " + room.getRoomNumber() + " - " + getStayLabel();
    }
}