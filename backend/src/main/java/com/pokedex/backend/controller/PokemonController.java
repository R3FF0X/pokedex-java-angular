package com.pokedex.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.pokedex.backend.service.PokemonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pokemon")
@RequiredArgsConstructor
public class PokemonController {

    private final PokemonService pokemonService;

    @GetMapping
    public ResponseEntity<JsonNode> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(pokemonService.getPokemonList(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<JsonNode> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(pokemonService.searchPokemon(q, page, size));
    }

    @GetMapping("/{nameOrId}")
    public ResponseEntity<JsonNode> getDetail(@PathVariable String nameOrId) {
        return ResponseEntity.ok(pokemonService.getPokemonDetail(nameOrId));
    }
}