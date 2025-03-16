package com.dnd.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PartyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "party_characters", joinColumns = @JoinColumn(name = "party_id"), inverseJoinColumns = @JoinColumn(name = "character_id"))
    private List<CharacterEntity> members = new ArrayList<>();

    // ✅ Getters & Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<CharacterEntity> getMembers() {
        return members;
    }

    public void setName(String name) {
        this.name = name;
    }
}
