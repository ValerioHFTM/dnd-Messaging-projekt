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

public class MobSpawnPanel extends JFrame {
    private JComboBox<String> partyDropdown, raceDropdown;
    private JButton spawnButton;
    private JTextArea mobSpawnList;
    private static final String BASE_URL = "http://localhost:8080/mob";
    private static final String PARTY_URL = "http://localhost:8080/party";
    private static final String MOB_RACE_URL = "http://localhost:8080/mob-race";

    public MobSpawnPanel() {
        setTitle("Spawn a Mob");
        setSize(500, 400);
        setLayout(new BorderLayout());

        // **Top panel for party and mob selection**
        JPanel topPanel = new JPanel();
        partyDropdown = new JComboBox<>();
        raceDropdown = new JComboBox<>();
        spawnButton = new JButton("Spawn Mob");

        topPanel.add(new JLabel("Select Party:"));
        topPanel.add(partyDropdown);
        topPanel.add(new JLabel("Select Mob Race:"));
        topPanel.add(raceDropdown);
        topPanel.add(spawnButton);
        add(topPanel, BorderLayout.NORTH);

        // **Text area for displaying spawned mobs**
        mobSpawnList = new JTextArea(10, 40);
        mobSpawnList.setEditable(false);
        add(new JScrollPane(mobSpawnList), BorderLayout.CENTER);

        // Load data
        loadParties();
        loadMobRaces();
        loadMobs();

        // **Spawn action**
        spawnButton.addActionListener(e -> spawnMob());

        setVisible(true);
    }

    private void loadParties() {
        try {
            URL url = new URL(PARTY_URL);
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

            updatePartyDropdown(response.toString());
        } catch (Exception ex) {
            mobSpawnList.setText("Error loading parties!");
        }
    }

    private void updatePartyDropdown(String jsonResponse) {
        partyDropdown.removeAllItems();
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            partyDropdown.addItem(obj.getString("name"));
        }
    }

    private void loadMobRaces() {
        try {
            URL url = new URL(MOB_RACE_URL);
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

            updateRaceDropdown(response.toString());
        } catch (Exception ex) {
            mobSpawnList.setText("Error loading mob races!");
        }
    }

    private void updateRaceDropdown(String jsonResponse) {
        raceDropdown.removeAllItems();
        raceDropdown.addItem("Random");

        JSONArray jsonArray = new JSONArray(jsonResponse);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            raceDropdown.addItem(obj.getString("name"));
        }
    }

    private void spawnMob() {
        String party = (String) partyDropdown.getSelectedItem();
        String race = (String) raceDropdown.getSelectedItem();
        if (party == null || race == null) {
            JOptionPane.showMessageDialog(this, "Select a party and race!");
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/spawn/" + party + "/" + race);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                JOptionPane.showMessageDialog(this, "Spawned a " + race + "!");
                loadMobs();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to spawn mob!");
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Server connection error!");
        }
    }

    private void loadMobs() {
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

            mobSpawnList.setText(response.toString());
        } catch (Exception ex) {
            mobSpawnList.setText("Error loading mobs!");
        }
    }
}
