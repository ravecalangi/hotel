package com.mycompany.hotelmanagementsystem;

import java.awt.EventQueue;

/**
 * HotelManagementSystem - entry point of the application.
 * Creates the HotelManager (logic) and opens the HotelGUI (interface).
 */
public class HotelManagementSystem {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            HotelManager manager = new HotelManager();
            new HotelGUI(manager).setVisible(true);
        });
    }
}