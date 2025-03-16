package com.dnd.resource;

import com.dnd.entity.CharacterEntity;
import com.dnd.entity.PartyEntity;
import com.dnd.repository.CharacterRepository;
import com.dnd.repository.PartyRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/party")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartyResource {

    @Inject
    PartyRepository partyRepository;

    @Inject
    CharacterRepository characterRepository;

    // Create a new party
    @POST
    @Transactional
    public Response createParty(PartyEntity party) {
        partyRepository.persist(party);
        return Response.status(Response.Status.CREATED).entity(party).build();
    }

    // Get all parties
    @GET
    public List<PartyEntity> getAllParties() {
        return partyRepository.listAll();
    }

    // Get a specific party by name
    @GET
    @Path("/{partyName}")
    public Response getPartyByName(@PathParam("partyName") String partyName) {
        PartyEntity party = partyRepository.find("name", partyName).firstResult();
        if (party == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Party not found").build();
        }
        return Response.ok(party).build();
    }

    // Add a character to a party
    @PUT
    @Path("/{partyName}/add-character/{characterName}")
    @Transactional
    public Response addCharacterToParty(
            @PathParam("partyName") String partyName,
            @PathParam("characterName") String characterName) {

        PartyEntity party = partyRepository.find("name", partyName).firstResult();
        CharacterEntity character = characterRepository.find("name", characterName).firstResult();

        if (party == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Party not found").build();
        }
        if (character == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Character not found").build();
        }

        if (!party.getMembers().contains(character)) { // Avoid duplicates
            party.getMembers().add(character);
            partyRepository.persist(party); // ✅ Ensure persistence
        }

        return Response.ok(party).build();
    }

    // Remove a character from a party
    @PUT
    @Path("/{partyName}/remove-character/{characterName}")
    @Transactional
    public Response removeCharacterFromParty(
            @PathParam("partyName") String partyName,
            @PathParam("characterName") String characterName) {

        PartyEntity party = partyRepository.find("name", partyName).firstResult();
        CharacterEntity character = characterRepository.find("name", characterName).firstResult();

        if (party == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Party not found").build();
        }
        if (character == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Character not found").build();
        }

        if (party.getMembers().contains(character)) {
            party.getMembers().remove(character);
            partyRepository.persist(party);
        }

        return Response.ok(party).build();
    }

    @DELETE
    @Path("/{partyName}")
    @Transactional
    public Response deleteParty(@PathParam("partyName") String partyName) {
        PartyEntity party = partyRepository.find("name", partyName).firstResult();
        if (party == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Party not found").build();
        }

        partyRepository.delete(party);
        return Response.ok().entity("Party " + partyName + " deleted").build();
    }

}
