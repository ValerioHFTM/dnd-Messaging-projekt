package com.dnd.resource;

import com.dnd.entity.MobRace;
import com.dnd.repository.MobRaceRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/mob-race")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MobRaceResource {

    @Inject
    MobRaceRepository repository;

    // GET: Get all mob races
    @GET
    public List<MobRace> getAllMobRaces() {
        return repository.listAll();
    }

    // POST: Add a new Mob Race
    @POST
    @Transactional
    public Response createMobRace(MobRace mobRace) {
        if (repository.find("name", mobRace.name).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT).entity("MobRace already exists").build();
        }
        repository.persist(mobRace);
        return Response.status(Response.Status.CREATED).entity(mobRace).build();
    }

    // DELETE: Remove a Mob Race by name
    @DELETE
    @Path("/{name}")
    @Transactional
    public Response deleteMobRace(@PathParam("name") String name) {
        MobRace mobRace = repository.find("name", name).firstResult();
        if (mobRace == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("MobRace not found").build();
        }
        repository.delete(mobRace);
        return Response.noContent().build();
    }
}
