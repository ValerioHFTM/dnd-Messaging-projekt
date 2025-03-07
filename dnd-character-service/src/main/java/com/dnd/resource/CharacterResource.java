package com.dnd.resource;

import com.dnd.entity.CharacterEntity;
import com.dnd.repository.CharacterRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/characters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Character API", description = "Verwaltung von D&D-Charakteren")
public class CharacterResource {

    @Inject
    CharacterRepository repository;

    @GET
    public List<CharacterEntity> getAllCharacters() {
        return repository.listAll();
    }

    @GET
    @Path("/name/{characterName}")
    @Operation(summary = "Charakter nach Name abrufen", description = "Gibt den Charakter anhand des Namens zurück")
    public CharacterEntity getCharacterByName(@PathParam("characterName") String characterName) {
        return repository.find("name", characterName).firstResult();
    }

    @POST
    @Transactional
    public Response createCharacter(CharacterEntity character) {
        repository.persistAndFlush(character);
        return Response.status(Response.Status.CREATED).entity(character).build();
    }

    @PUT
    @Path("/{id}/level-up")
    @Transactional
    public Response levelUpCharacter(@PathParam("id") Long id) {
        CharacterEntity character = repository.findById(id);
        if (character == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        character.level += 1;
        return Response.ok(character).build();
    }

    @PUT
    @Path("/levelup/{characterName}")
    @Transactional
    @Operation(summary = "Charakter leveln", description = "Erhöht das Level eines Charakters anhand des Namens")
    public Response levelUpCharacter(@PathParam("characterName") String characterName) {
        CharacterEntity character = repository.find("name", characterName).firstResult();

        if (character == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Character not found").build();
        }

        character.level++;
        String event = character.name + " leveled up to " + character.level;
        levelUpEmitter.send(event);
        return Response.ok(character).build();
    }

    @Inject
    @Channel("level-up")
    Emitter<String> levelUpEmitter;
}
