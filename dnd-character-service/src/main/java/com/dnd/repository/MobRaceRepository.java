package com.dnd.repository;

import com.dnd.entity.MobRace;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MobRaceRepository implements PanacheRepository<MobRace> {

}
