package com.dnd.repository;

import com.dnd.entity.PartyEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PartyRepository implements PanacheRepository<PartyEntity> {
    // Panache automatically provides basic CRUD operations
}
