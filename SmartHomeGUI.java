package com.mycompany.smarthome;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SmartHomeGUI provides a graphical interface for controlling SmartHome services.
 */
public class SmartHomeGUI extends JFrame {

    public SmartHomeGUI() {
        setTitle("SmartHome Controller");
        setSize(400, 300);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel setup
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));

        // Buttons for each service
        JButton climateBtn = new JButton("🌡️ Set Climate Temperature");
        JButton energyBtn = new JButton("🔌 View Energy Usage");
        JButton plantBtn = new JButton("🌱 Send Plant Sensor Readings");
        JButton securityBtn = new JButton("🚪 Monitor Door Events");
        JButton exitBtn = new JButton("Exit");

        // Add buttons to the panel
        panel.add(climateBtn);
        panel.add(energyBtn);
        panel.add(plantBtn);
        panel.add(securityBtn);
        panel.add(exitBtn);
        add(panel);

        // Action handlers

        // Climate control client (does not throw exceptions)
        climateBtn.addActionListener(e -> ClimateControlClient.main(null));

        // Energy client (does not throw exceptions)
        energyBtn.addActionListener(e -> EnergyRoutineClient.main(null));

        // Plant sensor client (throws InterruptedException)
        plantBtn.addActionListener(e -> {
            try {
                PlantSensorClient.main(null);
            } catch (InterruptedException ex) {
                Logger.getLogger(SmartHomeGUI.class.getName()).log(Level.SEVERE, "Error in Plant Sensor Client", ex);
                Thread.currentThread().interrupt();
            }
        });

        // Security client (throws InterruptedException)
        securityBtn.addActionListener(e -> {
            try {
                SecurityClient.main(null);
            } catch (InterruptedException ex) {
                Logger.getLogger(SmartHomeGUI.class.getName()).log(Level.SEVERE, "Error in Security Client", ex);
                Thread.currentThread().interrupt();
            }
        });

        // Exit button
        exitBtn.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmartHomeGUI gui = new SmartHomeGUI();
            gui.setVisible(true);
        });
    }
}
