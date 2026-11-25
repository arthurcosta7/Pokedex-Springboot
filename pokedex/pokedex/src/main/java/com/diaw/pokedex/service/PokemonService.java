package com.diaw.pokedex.service;

import java.util.List;
import com.diaw.pokedex.dto.PokemonRequest;
import com.diaw.pokedex.model.Pokemon;
import com.diaw.pokedex.model.PokemonType;
import com.diaw.pokedex.model.Trainer;
import com.diaw.pokedex.repository.PokemonRepository;
import com.diaw.pokedex.repository.PokemonTypeRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PokemonService {
    private final PokemonRepository repository;
    private final PokemonTypeRepository types;
    private final MongoTemplate mongo;

    public PokemonService(PokemonRepository repository, PokemonTypeRepository types, MongoTemplate mongo) {
        this.repository = repository;
        this.types = types;
        this.mongo = mongo;
    }

    public List<Pokemon> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    public Pokemon findById(int id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    public Pokemon create(PokemonRequest request) {
        // insert, em vez de save, impede sobrescrever um ID existente.
        return repository.insert(toPokemon(request));
    }

    public Pokemon update(int id, PokemonRequest request) {
        if (id != request.id()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do corpo deve ser igual ao ID da URL.");
        }
        Pokemon replacement = toPokemon(request);
        // Substitui somente se existir; nunca faz upsert em um PUT desconhecido.
        Pokemon previous = mongo.findAndReplace(byId(id), replacement);
        if (previous == null) throw notFound(id);
        return replacement;
    }

    public void delete(int id) {
        Pokemon removed = mongo.findAndRemove(byId(id), Pokemon.class);
        if (removed == null) throw notFound(id);
        // $pull remove todas as ocorrências, inclusive equipes com Pokémon repetidos.
        mongo.updateMulti(Query.query(Criteria.where("pokemonIds").is(id)),
                new Update().pull("pokemonIds", id), Trainer.class);
    }

    private Pokemon toPokemon(PokemonRequest request) {
        if (request.typeIds().stream().distinct().count() != request.typeIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não repita os tipos do Pokémon.");
        }
        List<PokemonType> resolved = request.typeIds().stream().map(id -> types.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tipo não encontrado: " + id))).toList();
        return new Pokemon(request.id(), request.name().trim(), request.stats(), resolved);
    }

    private Query byId(int id) {
        return Query.query(Criteria.where("_id").is(id));
    }

    private ResponseStatusException notFound(int id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokémon não encontrado: " + id);
    }
}
