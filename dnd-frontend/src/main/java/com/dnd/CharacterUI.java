package com.dnd;

import javax.swing.*;
import java.awt.*;

public class CharacterUI extends JFrame {

    public CharacterUI() {
        setTitle("DND Charakter Verwaltung");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLayout(new BorderLayout());

        // Panel für Buttons
        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Charakter erstellen");
        JButton showButton = new JButton("Charaktere anzeigen");
        JButton partyButton = new JButton("Party verwalten");

        createButton.addActionListener(e -> openCharacterCreatePanel());
        showButton.addActionListener(e -> openCharacterListPanel());
        partyButton.addActionListener(e -> openPartyManagementPanel());

        buttonPanel.add(createButton);
        buttonPanel.add(showButton);
        buttonPanel.add(partyButton);

        add(buttonPanel, BorderLayout.NORTH);
        setVisible(true);
    }

    private void openCharacterCreatePanel() {
        new CharacterCreatePanel();
    }

    private void openCharacterListPanel() {
        new CharacterListPanel();
    }

    private void openPartyManagementPanel() {
        new PartyManagementPanel();
    }

    public static void main(String[] args) {
        new CharacterUI();
    }
}
