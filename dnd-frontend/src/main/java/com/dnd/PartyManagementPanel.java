package com.dnd;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PartyManagementPanel extends JFrame {
    private JComboBox<String> partyDropdown;
    private JComboBox<String> characterDropdown;
    private JComboBox<String> partyMembersDropdown;
    private JTextArea partyMembersArea;
    private JButton addCharacterButton;
    private JButton removeCharacterButton;
    private JButton deletePartyButton;
    private JTextField partyNameField;
    private JButton createPartyButton;

    private static final String BASE_URL = "http://localhost:8080";

    public PartyManagementPanel() {
        setTitle("Party-Verwaltung");
        setSize(500, 550);
        setLayout(new BorderLayout());

        // Panel für Party-Auswahl & Buttons
        JPanel topPanel = new JPanel();
        partyDropdown = new JComboBox<>();
        characterDropdown = new JComboBox<>();
        partyMembersDropdown = new JComboBox<>();
        addCharacterButton = new JButton("Charakter hinzufügen");
        removeCharacterButton = new JButton("Charakter entfernen");
        deletePartyButton = new JButton("Party löschen");
        partyNameField = new JTextField(15);
        createPartyButton = new JButton("Party erstellen");
        createPartyButton.addActionListener(e -> createParty());

        addCharacterButton.addActionListener(e -> addCharacterToParty());
        removeCharacterButton.addActionListener(e -> removeCharacterFromParty());
        deletePartyButton.addActionListener(e -> deleteParty());

        topPanel.add(new JLabel("Party:"));
        topPanel.add(partyDropdown);
        topPanel.add(deletePartyButton);
        topPanel.add(new JLabel("Charakter:"));
        topPanel.add(characterDropdown);
        topPanel.add(addCharacterButton);
        topPanel.add(new JLabel("Neue Party:"));
        topPanel.add(partyNameField);
        topPanel.add(createPartyButton);

        add(topPanel, BorderLayout.NORTH);

        // Party-Mitglieder Panel
        JPanel middlePanel = new JPanel();
        middlePanel.add(new JLabel("Mitglied entfernen:"));
        middlePanel.add(partyMembersDropdown);
        middlePanel.add(removeCharacterButton);

        add(middlePanel, BorderLayout.CENTER);

        // Bereich für Party-Mitglieder-Anzeige
        partyMembersArea = new JTextArea(15, 40);
        partyMembersArea.setEditable(false);
        partyMembersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(partyMembersArea), BorderLayout.SOUTH);

        // Event für Dropdown-Änderung: Party-Mitglieder neu laden
        partyDropdown.addActionListener(e -> loadPartyMembers());

        loadParties();
        loadCharacters();
        setVisible(true);
    }

    private void loadParties() {
        try {
            URL url = new URL(BASE_URL + "/party");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();
            connection.disconnect();

            updatePartyDropdown(response);
        } catch (Exception ex) {
            partyMembersArea.setText("Fehler beim Abrufen der Partys!");
        }
    }

    private void updatePartyDropdown(String jsonResponse) {
        partyDropdown.removeAllItems();
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            partyDropdown.addItem(obj.getString("name"));
        }
        loadPartyMembers(); // Direkt die Mitglieder der ersten Party laden
    }

    private void loadCharacters() {
        try {
            URL url = new URL(BASE_URL + "/characters");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();
            connection.disconnect();

            updateCharacterDropdown(response);
        } catch (Exception ex) {
            partyMembersArea.setText("Fehler beim Abrufen der Charaktere!");
        }
    }

    private void updateCharacterDropdown(String jsonResponse) {
        characterDropdown.removeAllItems();
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            characterDropdown.addItem(obj.getString("name"));
        }
    }

    private void loadPartyMembers() {
        String selectedParty = (String) partyDropdown.getSelectedItem();
        if (selectedParty == null) {
            partyMembersArea.setText("Keine Party ausgewählt.");
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/party/" + selectedParty.replace(" ", "%20"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            reader.close();
            connection.disconnect();

            updatePartyInfo(response);
        } catch (Exception ex) {
            partyMembersArea.setText("Fehler beim Abrufen der Party-Mitglieder!");
        }
    }

    private void updatePartyInfo(String jsonResponse) {
        StringBuilder infoText = new StringBuilder("Party-Mitglieder:\n\n");
        JSONObject partyObject = new JSONObject(jsonResponse);
        JSONArray members = partyObject.getJSONArray("members");

        partyMembersDropdown.removeAllItems();

        if (members.length() == 0) {
            infoText.append("Diese Party hat noch keine Mitglieder.");
        } else {
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);
                String memberName = member.getString("name");
                partyMembersDropdown.addItem(memberName);

                infoText.append("Name: ").append(memberName)
                        .append("\nLevel: ").append(member.getInt("level"))
                        .append("\nStrength: ").append(member.getInt("strength"))
                        .append("\nDexterity: ").append(member.getInt("dexterity"))
                        .append("\nIntelligence: ").append(member.getInt("intelligence"))
                        .append("\n--------------------\n");
            }
        }
        partyMembersArea.setText(infoText.toString());
    }

    private void addCharacterToParty() {
        String selectedParty = (String) partyDropdown.getSelectedItem();
        String selectedCharacter = (String) characterDropdown.getSelectedItem();

        if (selectedParty == null || selectedCharacter == null) {
            JOptionPane.showMessageDialog(this, "Bitte eine Party und einen Charakter auswählen!");
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/party/" + selectedParty.replace(" ", "%20") + "/add-character/"
                    + selectedCharacter.replace(" ", "%20"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this,
                        selectedCharacter + " wurde zur Party " + selectedParty + " hinzugefügt!");
                loadPartyMembers(); // Mitglieder aktualisieren
            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Hinzufügen des Charakters zur Party!");
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Verbinden mit dem Server!");
        }
    }

    private void removeCharacterFromParty() {

        String selectedParty = (String) partyDropdown.getSelectedItem();
        String selectedCharacter = (String) partyMembersDropdown.getSelectedItem();

        if (selectedParty == null || selectedCharacter == null) {
            JOptionPane.showMessageDialog(this, "Bitte eine Party und ein Mitglied auswählen!");
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/party/" + selectedParty.replace(" ", "%20") + "/remove-character/"
                    + selectedCharacter.replace(" ", "%20"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, selectedCharacter + " wurde aus der Party entfernt!");
                loadPartyMembers(); // Mitglieder aktualisieren
            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Entfernen!");
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Server!");
        }
    }

    private void deleteParty() {
        String selectedParty = (String) partyDropdown.getSelectedItem();

        if (selectedParty == null) {
            JOptionPane.showMessageDialog(this, "Bitte eine Party auswählen!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Willst du die Party '" + selectedParty + "' wirklich löschen?",
                "Party löschen", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/party/" + selectedParty.replace(" ", "%20"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == 200) {
                JOptionPane.showMessageDialog(this, "Party '" + selectedParty + "' wurde gelöscht!");
                loadCharacters(); // Refresh party list
                loadParties(); // Refresh the party dropdown immediately

            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Löschen der Party!");
                loadCharacters(); // Refresh party list
                loadParties();
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Verbinden mit dem Server!");
        }
    }

    private void createParty() {
        String partyName = partyNameField.getText();
        if (partyName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte einen Namen für die Party eingeben!");
            return;
        }

        try {
            URL url = new URL(BASE_URL + "/party");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"name\":\"" + partyName + "\"}";
            connection.getOutputStream().write(jsonInputString.getBytes());

            int responseCode = connection.getResponseCode();

            System.out.println("Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(this, "Party " + partyName + " wurde erstellt!");
                partyNameField.setText(""); // Clear input
                loadCharacters(); // Refresh party list
                loadParties(); // Refresh the party dropdown immediately

            } else {
                JOptionPane.showMessageDialog(this, "Party");
                loadParties();
            }
            connection.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Verbinden mit dem Server!");
        }
    }
}
