package com.mycompany.hotelmanagementsystem;

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


    public abstract String getRoomType();


    public abstract String getAmenities();


    public abstract double calculateTotal(int nights);

    @Override
    public String toString() {
        return String.format("Room %d  -  %s  -  PHP %,.0f / night",
                roomNumber, getRoomType(), ratePerNight);
    }
}


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
        return getRatePerNight() * nights * 1.05; 
    }
}


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
            subtotal *= 0.90;
        }
        return subtotal + CONCIERGE_FEE;
    }
}
