package com.diaw.pokedex.repository;

import java.util.List;
import com.diaw.pokedex.model.PokemonType;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PokemonTypeRepository extends MongoRepository<PokemonType, Integer> {
    List<PokemonType> findAllByOrderByIdAsc();
}
