package com.diaw.pokedex.repository;

import java.util.List;
import com.diaw.pokedex.model.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrainerRepository extends MongoRepository<Trainer, Integer> {
    List<Trainer> findAllByOrderByIdAsc();
}
