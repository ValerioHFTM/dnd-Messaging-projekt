package com.dnd;

import javax.swing.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CharacterCreatePanel extends JFrame {
    private static final String BASE_URL = "http://localhost:8080/characters";

    public CharacterCreatePanel() {
        setTitle("Neuen Charakter erstellen");
        setSize(400, 200);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JTextField nameField = new JTextField(20);
        JButton createButton = new JButton("Erstellen");

        createButton.addActionListener(e -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                createCharacter(name);
                nameField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Bitte einen Namen eingeben!");
            }
        });

        add(new JLabel("Charaktername:"));
        add(nameField);
        add(createButton);

        setVisible(true);
    }

    private void createCharacter(String name) {
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{\"name\":\"" + name + "\", \"level\":1}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                JOptionPane.showMessageDialog(this, "Charakter " + name + " erstellt!");
            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Erstellen!");
            }

            conn.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Server nicht erreichbar!");
        }
    }
}