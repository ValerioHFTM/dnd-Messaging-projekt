package com.dnd.listener;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Random;

@ApplicationScoped
public class LevelUpListener {

    private final Random random = new Random();

    @Inject
    @Channel("level-up-result") // Sending boosted attribute back
    Emitter<String> resultEmitter;

    @Incoming("level-up")
    public void onCharacterLevelUp(String message) {
        System.out.println("Received Kafka message: " + message);

        // Simulierte Attribut-Erhöhung
        String[] attributes = { "Strength", "Dexterity", "Intelligence" };
        String boostedAttribute = attributes[random.nextInt(attributes.length)];

        String resultMessage = message + " - Boosted Attribute: " + boostedAttribute;
        resultEmitter.send(resultMessage);

        System.out.println("Boosting " + boostedAttribute);
    }
}
