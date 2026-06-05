package com.pokedex.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PokemonService {

    private final RestTemplate restTemplate;
    private static final String POKEAPI_URL = "https://pokeapi.co/api/v2";

    public JsonNode getPokemonList(int page, int size) {
        int offset = page * size;
        String url = UriComponentsBuilder
                .fromHttpUrl(POKEAPI_URL + "/pokemon")
                .queryParam("limit", size)
                .queryParam("offset", offset)
                .toUriString();
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public JsonNode getPokemonDetail(String nameOrId) {
        String url = POKEAPI_URL + "/pokemon/" + nameOrId;
        return restTemplate.getForObject(url, JsonNode.class);
    }
}