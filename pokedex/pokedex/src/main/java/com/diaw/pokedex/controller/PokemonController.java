package com.diaw.pokedex.controller;

import java.net.URI;
import java.util.List;
import com.diaw.pokedex.dto.PokemonRequest;
import com.diaw.pokedex.model.Pokemon;
import com.diaw.pokedex.service.PokemonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {
    private final PokemonService service;

    public PokemonController(PokemonService service) { this.service = service; }

    @GetMapping
    public List<Pokemon> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public Pokemon findById(@PathVariable int id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<Pokemon> create(@Valid @RequestBody PokemonRequest request) {
        Pokemon created = service.create(request);
        return ResponseEntity.created(URI.create("/api/pokemons/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public Pokemon update(@PathVariable int id, @Valid @RequestBody PokemonRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
