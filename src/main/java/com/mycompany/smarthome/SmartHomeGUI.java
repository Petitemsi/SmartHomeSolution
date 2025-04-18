package com.mycompany.smarthome;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SmartHomeGUI provides a graphical interface for controlling SmartHome services.
 */
public class SmartHomeGUI extends JFrame {
    private JTextArea outputArea; // Add this field

    public SmartHomeGUI() {
        setTitle("SmartHome Controller");
        setSize(600, 500); // Increased window size
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Button panel setup
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 10, 10));

        // Buttons for each service
        JButton climateBtn = new JButton("🌡️ Set Climate Temperature");
        JButton energyBtn = new JButton("🔌 View Energy Usage");
        JButton plantBtn = new JButton("🌱 Send Plant Sensor Readings");
        JButton securityBtn = new JButton("🚪 Monitor Door Events");
        JButton exitBtn = new JButton("Exit");

        // Add buttons to the button panel
        buttonPanel.add(climateBtn);
        buttonPanel.add(energyBtn);
        buttonPanel.add(plantBtn);
        buttonPanel.add(securityBtn);
        buttonPanel.add(exitBtn);

        // Create output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(300, 400));

        // Add components to main panel
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);

        // Modify action handlers to include output
        climateBtn.addActionListener(e -> {
            appendToOutput("Connecting to Climate Control Service...\n");
            ClimateControlClient.main(null);
        });

        energyBtn.addActionListener(e -> {
            appendToOutput("Connecting to Energy Service...\n");
            EnergyRoutineClient.main(null);
        });

        plantBtn.addActionListener(e -> {
            try {
                appendToOutput("Connecting to Plant Sensor Service...\n");
                PlantSensorClient.main(null);
            } catch (InterruptedException ex) {
                appendToOutput("Error: " + ex.getMessage() + "\n");
                Logger.getLogger(SmartHomeGUI.class.getName()).log(Level.SEVERE, "Error in Plant Sensor Client", ex);
                Thread.currentThread().interrupt();
            }
        });

        securityBtn.addActionListener(e -> {
            try {
                appendToOutput("Connecting to Security Service...\n");
                SecurityClient.main(null);
            } catch (InterruptedException ex) {
                appendToOutput("Error: " + ex.getMessage() + "\n");
                Logger.getLogger(SmartHomeGUI.class.getName()).log(Level.SEVERE, "Error in Security Client", ex);
                Thread.currentThread().interrupt();
            }
        });

        exitBtn.addActionListener(e -> System.exit(0));
    }

    // Add this method to append text to the output area
    public void appendToOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmartHomeGUI gui = new SmartHomeGUI();
            gui.setVisible(true);
        });
    }
}
