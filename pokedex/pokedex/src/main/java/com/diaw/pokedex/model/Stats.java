package com.diaw.pokedex.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record Stats(
        @NotNull @Min(0) @Max(255) Integer hp,
        @NotNull @Min(0) @Max(255) Integer attack,
        @NotNull @Min(0) @Max(255) Integer defense,
        @NotNull @Min(0) @Max(255) Integer specialAttack,
        @NotNull @Min(0) @Max(255) Integer specialDefense,
        @NotNull @Min(0) @Max(255) Integer speed) {
}
