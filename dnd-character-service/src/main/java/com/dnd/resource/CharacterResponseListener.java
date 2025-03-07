package com.dnd.resource;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dnd.entity.CharacterEntity;
import com.dnd.repository.CharacterRepository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CharacterResponseListener {

    @Inject
    CharacterRepository repository;

    @Incoming("level-up-result") // Listening to Kafka topic
    @Transactional // Required for database updates
    public void onLevelUpResult(String message) {
        System.out.println("Received Level-Up Result: " + message);

        // Example message format: "Thalindor leveled up to 6 - Boosted Attribute:
        // Dexterity"
        String[] parts = message.split(" - Boosted Attribute: ");
        if (parts.length != 2) {
            System.out.println("Invalid message format: " + message);
            return;
        }

        String characterName = parts[0].split(" leveled up to ")[0].trim();
        String boostedAttribute = parts[1].trim().toLowerCase();

        // Find the character in the database
        CharacterEntity character = repository.find("name", characterName).firstResult();
        if (character != null) {
            // Update the correct attribute
            switch (boostedAttribute) {
                case "strength":
                    character.strength++;
                    break;
                case "dexterity":
                    character.dexterity++;
                    break;
                case "intelligence":
                    character.intelligence++;
                    break;
                default:
                    System.out.println("Invalid attribute received: " + boostedAttribute);
                    return;
            }

            // Save updated character
            repository.persist(character);
            System.out.println("Updated " + boostedAttribute + " for " + characterName);
        } else {
            System.out.println("Character not found: " + characterName);
        }
    }
}
