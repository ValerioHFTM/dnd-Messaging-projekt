package com.dnd.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mobs")
public class MobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public int level;

    @Column(nullable = false)
    public String race;

    public MobEntity() {
    }

    public MobEntity(String name, int level, String race) {
        this.name = name;
        this.level = level;
        this.race = race;
    }
}
