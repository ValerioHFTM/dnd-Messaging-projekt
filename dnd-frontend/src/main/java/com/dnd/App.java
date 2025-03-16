package com.dnd;

import javax.swing.SwingUtilities;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        SwingUtilities.invokeLater(CharacterUI::new);
    }
}
