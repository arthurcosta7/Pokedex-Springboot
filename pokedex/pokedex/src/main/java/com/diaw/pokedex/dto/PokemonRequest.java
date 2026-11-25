package com.diaw.pokedex.dto;

import java.util.List;
import com.diaw.pokedex.model.Stats;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record PokemonRequest(
        @NotNull @Positive Integer id,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Valid Stats stats,
        @NotNull @Size(max = 2) List<@NotNull @Min(0) Integer> typeIds) {
}
