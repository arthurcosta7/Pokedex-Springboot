package com.diaw.pokedex.controller;

import java.util.List;
import com.diaw.pokedex.model.PokemonType;
import com.diaw.pokedex.model.Trainer;
import com.diaw.pokedex.repository.PokemonTypeRepository;
import com.diaw.pokedex.repository.TrainerRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final PokemonTypeRepository types;
    private final TrainerRepository trainers;

    public CatalogController(PokemonTypeRepository types, TrainerRepository trainers) {
        this.types = types;
        this.trainers = trainers;
    }

    @GetMapping("/types")
    public List<PokemonType> types() { return types.findAllByOrderByIdAsc(); }

    @GetMapping("/trainers")
    public List<Trainer> trainers() { return trainers.findAllByOrderByIdAsc(); }
}
