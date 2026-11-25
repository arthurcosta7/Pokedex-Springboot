package com.diaw.pokedex.service;

import java.util.List;
import java.util.Optional;
import com.diaw.pokedex.dto.PokemonRequest;
import com.diaw.pokedex.model.*;
import com.diaw.pokedex.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class PokemonServiceTests {
    final PokemonRepository repository = mock(PokemonRepository.class);
    final PokemonTypeRepository types = mock(PokemonTypeRepository.class);
    final MongoTemplate mongo = mock(MongoTemplate.class);
    final PokemonService service = new PokemonService(repository, types, mongo);
    final Stats stats = new Stats(35, 55, 30, 50, 40, 90);
    final PokemonType electric = new PokemonType(5, "Electric");
    final Pokemon pokemon = new Pokemon(25, "Pikachu", stats, List.of(electric));

    @Test void createUsesCanonicalTypesAndDoesNotOverwrite() {
        when(types.findById(5)).thenReturn(Optional.of(electric));
        when(repository.insert(any(Pokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Pokemon result = service.create(new PokemonRequest(25, " Pikachu ", stats, List.of(5)));
        assertEquals(pokemon, result);
        verify(repository).insert(pokemon);
        verify(repository, never()).save(any(Pokemon.class));
    }

    @Test void rejectsUnknownOrRepeatedTypes() {
        when(types.findById(99)).thenReturn(Optional.empty());
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.create(new PokemonRequest(25, "Pikachu", stats, List.of(99)))).getStatusCode().value());
        assertThrows(ResponseStatusException.class,
                () -> service.create(new PokemonRequest(25, "Pikachu", stats, List.of(5, 5))));
        verifyNoInteractions(repository);
    }

    @Test void missingDetailReturns404() {
        when(repository.findById(25)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.findById(25)).getStatusCode().value());
    }

    @Test void updateRejectsDifferentIds() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.update(26, new PokemonRequest(25, "Pikachu", stats, List.of()))).getStatusCode().value());
        verifyNoInteractions(mongo);
    }

    @Test void updateMissingPokemonDoesNotInsert() {
        when(mongo.findAndReplace(any(Query.class), any(Pokemon.class))).thenReturn(null);
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.update(25, new PokemonRequest(25, "Pikachu", stats, List.of()))).getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test void deleteRemovesTrainerReferences() {
        when(mongo.findAndRemove(any(Query.class), eq(Pokemon.class))).thenReturn(pokemon);
        service.delete(25);
        verify(mongo).updateMulti(argThat(query -> query.getQueryObject().get("pokemonIds").equals(25)),
                argThat(update -> update.getUpdateObject().toJson().contains("$pull")), eq(Trainer.class));
    }

    @Test void deleteMissingPokemonDoesNotChangeTrainers() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.delete(25)).getStatusCode().value());
        verify(mongo, never()).updateMulti(any(Query.class), any(Update.class), eq(Trainer.class));
    }
}
