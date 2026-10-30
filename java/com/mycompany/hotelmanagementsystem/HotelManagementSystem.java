package com.mycompany.hotelmanagementsystem;

import java.awt.EventQueue;

public class HotelManagementSystem {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            HotelManager manager = new HotelManager();
            new HotelGUI(manager).setVisible(true);
        });
    }
}
