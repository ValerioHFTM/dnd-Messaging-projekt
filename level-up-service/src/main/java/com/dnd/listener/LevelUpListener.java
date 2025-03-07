package com.dnd.listener;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Random;

@ApplicationScoped
public class LevelUpListener {

    private final Random random = new Random();

    @Incoming("level-up")
    public void onCharacterLevelUp(String message) {
        System.out.println("Received Kafka message: " + message);

        // Simulierte Attribut-Erhöhung
        String[] attributes = { "Strength", "Dexterity", "Intelligence" };
        String boostedAttribute = attributes[random.nextInt(attributes.length)];

        System.out.println("Boosting " + boostedAttribute);
    }
}
