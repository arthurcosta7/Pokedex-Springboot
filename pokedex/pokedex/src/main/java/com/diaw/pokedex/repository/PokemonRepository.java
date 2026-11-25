package com.diaw.pokedex.repository;

import java.util.List;
import com.diaw.pokedex.model.Pokemon;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PokemonRepository extends MongoRepository<Pokemon, Integer> {
    List<Pokemon> findAllByOrderByIdAsc();
}
