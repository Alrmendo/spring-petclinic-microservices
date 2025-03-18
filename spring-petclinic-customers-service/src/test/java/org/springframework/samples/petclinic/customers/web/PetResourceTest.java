package org.springframework.samples.petclinic.customers.web;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PetRepository petRepository;

    @MockBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetAPetInJSonFormat() throws Exception {
        Pet pet = setupPet(2, "Basil", 6);
        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    @Test
    void shouldReturnNotFoundForNonExistentPet() throws Exception {
        given(petRepository.findById(99)).willReturn(Optional.empty());

        mvc.perform(get("/owners/2/pets/99").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetListOfPetsForOwner() throws Exception {
        Owner owner = new Owner();
        Pet pet = setupPet(2, "Basil", 6);
        owner.addPet(pet);

        when(ownerRepository.findById(2)).thenReturn(Optional.of(owner));

        mvc.perform(get("/owners/2/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].name").value("Basil"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentOwner() throws Exception {
        when(ownerRepository.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/owners/99/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetMultiplePetsForOwner() throws Exception {
        Owner owner = new Owner();

        Pet pet1 = setupPet(10, "Fluffy", 4);
        Pet pet2 = setupPet(11, "Rex", 5);
        owner.addPet(pet1);
        owner.addPet(pet2);

        when(ownerRepository.findById(3)).thenReturn(Optional.of(owner));

        mvc.perform(get("/owners/3/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].name").value("Fluffy"))
            .andExpect(jsonPath("$[1].id").value(11))
            .andExpect(jsonPath("$[1].name").value("Rex"));
    }

    @Test
    void shouldGetPetWithDifferentType() throws Exception {
        Pet pet = setupPet(5, "Snowball", 7);
        given(petRepository.findById(5)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/5").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Snowball"))
            .andExpect(jsonPath("$.type.id").value(7));
    }

    private Pet setupPet(int petId, String name, int typeId) {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();
        pet.setName(name);
        pet.setId(petId);

        PetType petType = new PetType();
        petType.setId(typeId);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }
}
