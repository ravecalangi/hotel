package com.mycompany.hotelmanagementsystem;

/**
 * Room - abstract base class for every room in the hotel.
 * ABSTRACTION: subclasses must define their own type, amenities and pricing.
 * ENCAPSULATION: all fields are private and exposed only through getters.
 */
public abstract class Room {

    private final int roomNumber;
    private final double ratePerNight;
    private final int capacity;

    protected Room(int roomNumber, double ratePerNight, int capacity) {
        this.roomNumber = roomNumber;
        this.ratePerNight = ratePerNight;
        this.capacity = capacity;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public double getRatePerNight() {
        return ratePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    /** Name of the room category (e.g. "Standard"). */
    public abstract String getRoomType();

    /** Short description of what the room includes. */
    public abstract String getAmenities();

    /** Total cost for a stay; each room type applies its own pricing rule. */
    public abstract double calculateTotal(int nights);

    @Override
    public String toString() {
        return String.format("Room %d  -  %s  -  PHP %,.0f / night",
                roomNumber, getRoomType(), ratePerNight);
    }
}

/** INHERITANCE: Standard room - plain rate x nights. */
class StandardRoom extends Room {

    StandardRoom(int roomNumber, double ratePerNight, int capacity) {
        super(roomNumber, ratePerNight, capacity);
    }

    @Override
    public String getRoomType() {
        return "Standard";
    }

    @Override
    public String getAmenities() {
        return "Queen bed, Wi-Fi, air conditioning";
    }

    @Override
    public double calculateTotal(int nights) {
        return getRatePerNight() * nights;
    }
}

/** INHERITANCE: Deluxe room - adds a 5% service charge. */
class DeluxeRoom extends Room {

    DeluxeRoom(int roomNumber, double ratePerNight, int capacity) {
        super(roomNumber, ratePerNight, capacity);
    }

    @Override
    public String getRoomType() {
        return "Deluxe";
    }

    @Override
    public String getAmenities() {
        return "Double bed, city view, mini bar, breakfast";
    }

    @Override
    public double calculateTotal(int nights) {
        return getRatePerNight() * nights * 1.05; // 5% service charge
    }
}

/** INHERITANCE: Suite - flat concierge fee, 10% off for stays of 5+ nights. */
class SuiteRoom extends Room {

    private static final double CONCIERGE_FEE = 500;

    SuiteRoom(int roomNumber, double ratePerNight, int capacity) {
        super(roomNumber, ratePerNight, capacity);
    }

    @Override
    public String getRoomType() {
        return "Suite";
    }

    @Override
    public String getAmenities() {
        return "King bed, living area, jacuzzi, breakfast, concierge";
    }

    @Override
    public double calculateTotal(int nights) {
        double subtotal = getRatePerNight() * nights;
        if (nights >= 5) {
            subtotal *= 0.90; // long-stay discount
        }
        return subtotal + CONCIERGE_FEE;
    }
}