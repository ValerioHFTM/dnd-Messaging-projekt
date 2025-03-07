package com.dnd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class CharacterEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MariaDB-freundlich
    public Long id;

    @Column(nullable = false, unique = true)
    public String name;
    public int level;
    public int strength;
    public int dexterity;
    public int intelligence;

    public CharacterEntity() {
    }

    public CharacterEntity(String name, int level, int strength, int dexterity, int intelligence) {
        this.name = name;
        this.level = level;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
    }
}
