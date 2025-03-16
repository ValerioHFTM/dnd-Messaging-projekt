package com.dnd.repository;

import com.dnd.entity.MobEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MobRepository implements PanacheRepository<MobEntity> {
}
