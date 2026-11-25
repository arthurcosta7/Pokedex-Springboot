package com.diaw.pokedex.controller;

import java.util.List;
import com.diaw.pokedex.model.*;
import com.diaw.pokedex.service.PokemonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PokemonControllerTests {
    final PokemonService service = mock(PokemonService.class);
    MockMvc mvc;
    final Pokemon pokemon = new Pokemon(25, "Pikachu", new Stats(35, 55, 30, 50, 40, 90), List.of(new PokemonType(5, "Electric")));
    final String valid = """
        {"id":25,"name":"Pikachu","stats":{"hp":35,"attack":55,"defense":30,"specialAttack":50,"specialDefense":40,"speed":90},"typeIds":[5]}
        """;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new PokemonController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test void listAndDetail() throws Exception {
        when(service.findAll()).thenReturn(List.of(pokemon));
        when(service.findById(25)).thenReturn(pokemon);
        mvc.perform(get("/api/pokemons")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(25));
        mvc.perform(get("/api/pokemons/25")).andExpect(status().isOk()).andExpect(jsonPath("$.stats.hp").value(35));
    }

    @Test void createReturns201AndLocation() throws Exception {
        when(service.create(any())).thenReturn(pokemon);
        mvc.perform(post("/api/pokemons").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/pokemons/25"));
    }

    @Test void putAndDelete() throws Exception {
        when(service.update(eq(25), any())).thenReturn(pokemon);
        mvc.perform(put("/api/pokemons/25").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Pikachu"));
        mvc.perform(delete("/api/pokemons/25")).andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    @Test void invalidFieldsAndMissingStatsReturn400() throws Exception {
        for (String payload : List.of(valid.replace("\"hp\":35", "\"hp\":-1"),
                valid.replace("\"name\":\"Pikachu\"", "\"name\":\" \""),
                "{\"id\":25,\"name\":\"Pikachu\",\"typeIds\":[5]}")) {
            mvc.perform(post("/api/pokemons").contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").exists());
        }
        verifyNoInteractions(service);
    }

    @Test void missingAndDuplicateHaveUsefulStatusCodes() throws Exception {
        when(service.findById(999)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokémon não encontrado"));
        when(service.create(any())).thenThrow(new DuplicateKeyException("duplicate"));
        mvc.perform(get("/api/pokemons/999")).andExpect(status().isNotFound());
        mvc.perform(post("/api/pokemons").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("Já existe um Pokémon com esse ID."));
    }
}
