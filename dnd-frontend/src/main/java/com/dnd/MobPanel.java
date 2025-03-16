package com.dnd;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class MobPanel extends JFrame {
    private JComboBox<String> mobRaceDropdown;
    private JTextField mobRaceInput;
    private JButton addButton, deleteButton;
    private JTextArea mobRaceList;
    private static final String BASE_URL = "http://localhost:8080/mob-race";

    public MobPanel() {
        setTitle("Mob Race Management");
        setSize(500, 400);
        setLayout(new BorderLayout());

        // **Panel for input and buttons**
        JPanel topPanel = new JPanel();
        mobRaceInput = new JTextField(15);
        addButton = new JButton("Add Mob Race");
        deleteButton = new JButton("Delete Selected");

        topPanel.add(new JLabel("New Mob Race:"));
        topPanel.add(mobRaceInput);
        topPanel.add(addButton);
        topPanel.add(deleteButton);
        add(topPanel, BorderLayout.NORTH);

        // **Dropdown and text area for displaying Mob Races**
        mobRaceDropdown = new JComboBox<>();
        mobRaceList = new JTextArea(10, 40);
        mobRaceList.setEditable(false);

        JPanel centerPanel = new JPanel();
        centerPanel.add(new JLabel("Available Mob Races:"));
        centerPanel.add(mobRaceDropdown);
        add(centerPanel, BorderLayout.CENTER);
        add(new JScrollPane(mobRaceList), BorderLayout.SOUTH);

        // **Button actions**
        addButton.addActionListener(e -> addMobRace());
        deleteButton.addActionListener(e -> deleteMobRace());

        // Load existing mob races on startup
        loadMobRaces();

        setVisible(true);
    }

    private void loadMobRaces() {
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            updateMobRaceList(response.toString());
        } catch (Exception ex) {
            mobRaceList.setText("Error loading mob races!");
        }
    }

    private void updateMobRaceList(String jsonResponse) {
        mobRaceDropdown.removeAllItems();
        mobRaceList.setText("Mob Races:\n\n");

        JSONArray jsonArray = new JSONArray(jsonResponse);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String name = obj.getString("name");
            mobRaceDropdown.addItem(name);
            mobRaceList.append(name + "\n");
        }
    }

    private void addMobRace() {
        String name = mobRaceInput.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a valid Mob Race name!");
            return;
        }

        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"name\":\"" + name + "\"}";
            OutputStream os = connection.getOutputStream();
            os.write(jsonInputString.getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                JOptionPane.showMessageDialog(this, "Mob Race added: " + name);
                loadMobRaces(); // Refresh list
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add Mob Race!");
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Server connection error!");
        }
    }

    private void deleteMobRace() {
        String selectedRace = (String) mobRaceDropdown.getSelectedItem();
        if (selectedRace == null) {
            JOptionPane.showMessageDialog(this, "No Mob Race selected!");
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/" + selectedRace);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                JOptionPane.showMessageDialog(this, "Deleted: " + selectedRace);
                loadMobRaces(); // Refresh list
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete Mob Race!");
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Server connection error!");
        }
    }
}
