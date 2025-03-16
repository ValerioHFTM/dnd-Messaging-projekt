package com.dnd;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CharacterListPanel extends JFrame {
    private JComboBox<String> characterDropdown;
    private JTextArea characterInfoArea;
    private JButton levelUpButton;
    private static final String BASE_URL = "http://localhost:8080/characters";

    public CharacterListPanel() {
        setTitle("Charakterliste");
        setSize(500, 500);
        setLayout(new BorderLayout());

        // Panel für Charakterauswahl
        JPanel topPanel = new JPanel();
        characterDropdown = new JComboBox<>();
        levelUpButton = new JButton("Level Up");
        levelUpButton.addActionListener(e -> levelUpCharacter());

        topPanel.add(new JLabel("Wähle einen Charakter:"));
        topPanel.add(characterDropdown);
        topPanel.add(levelUpButton);
        add(topPanel, BorderLayout.NORTH);

        // Anzeige für Charakterdetails
        characterInfoArea = new JTextArea(15, 40);
        characterInfoArea.setEditable(false);
        characterInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(characterInfoArea), BorderLayout.CENTER);

        loadCharacterData();
        setVisible(true);
    }

    private void loadCharacterData() {
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

            updateCharacterDropdown(response.toString());
        } catch (Exception ex) {
            characterInfoArea.setText("Fehler beim Abrufen der Charaktere!");
        }
    }

    private void updateCharacterDropdown(String jsonResponse) {
        characterDropdown.removeAllItems();
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String name = obj.getString("name");
            characterDropdown.addItem(name);
        }
        updateCharacterInfo(jsonResponse);
    }

    private void updateCharacterInfo(String jsonResponse) {
        StringBuilder infoText = new StringBuilder("Charaktere:\n\n");
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            infoText.append("Name: ").append(obj.getString("name"))
                    .append("\nLevel: ").append(obj.getInt("level"))
                    .append("\nStrength: ").append(obj.getInt("strength"))
                    .append("\nDexterity: ").append(obj.getInt("dexterity"))
                    .append("\nIntelligence").append(obj.getInt("intelligence"))
                    .append("\n--------------------\n");
        }

        characterInfoArea.setText(infoText.toString());
    }

    private void levelUpCharacter() {
        String selectedCharacter = (String) characterDropdown.getSelectedItem();
        if (selectedCharacter == null) {
            JOptionPane.showMessageDialog(this, "Kein Charakter ausgewählt!");
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/levelup/" + selectedCharacter);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, selectedCharacter + " ist ein Level aufgestiegen!");
                loadCharacterData(); // Aktualisiert die Anzeige
            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Level-Up!");
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Verbinden mit dem Server!");
        }
    }
}
