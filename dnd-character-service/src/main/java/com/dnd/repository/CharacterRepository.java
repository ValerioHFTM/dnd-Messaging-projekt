package com.dnd.repository;

import com.dnd.entity.CharacterEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CharacterRepository implements PanacheRepository<CharacterEntity> {
    // Standard-CRUD-Methoden sind durch Panache bereits verfügbar!
}
