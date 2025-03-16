package com.dnd.resource;

import com.dnd.entity.MobEntity;
import com.dnd.entity.PartyEntity;
import com.dnd.repository.MobRepository;
import com.dnd.repository.PartyRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Random;

@Path("/mob")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MobResource {

    @Inject
    MobRepository mobRepository;

    @Inject
    PartyRepository partyRepository;

    private final Random random = new Random();

    @POST
    @Path("/spawn/{partyName}/{race}")
    @Transactional
    public Response spawnMob(@PathParam("partyName") String partyName, @PathParam("race") String race) {
        PartyEntity party = partyRepository.find("name", partyName).firstResult();

        if (party == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Party not found").build();
        }

        int averageLevel = party.members.stream().mapToInt(c -> c.level).sum() / Math.max(1, party.members.size());

        // If race is "Random", pick a random one from existing Mob Races
        if ("Random".equalsIgnoreCase(race)) {
            List<String> mobRaces = List.of("Goblin", "Orc", "Skeleton", "Dragon"); // Replace with real list from DB
            race = mobRaces.get(random.nextInt(mobRaces.size()));
        }

        MobEntity newMob = new MobEntity(race + " Lv." + averageLevel, averageLevel, race);
        mobRepository.persist(newMob);

        return Response.status(Response.Status.CREATED).entity(newMob).build();
    }

    @GET
    public List<MobEntity> getAllMobs() {
        return mobRepository.listAll();
    }
}
