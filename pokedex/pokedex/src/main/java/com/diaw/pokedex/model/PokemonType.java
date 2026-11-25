package com.diaw.pokedex.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "types")
public record PokemonType(@Id Integer id, String name) {
}
