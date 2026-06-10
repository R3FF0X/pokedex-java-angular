package com.pokedex.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PokemonService {

    private final RestTemplate restTemplate;
    private final PokemonCacheService cacheService;
    private final ObjectMapper objectMapper;
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
        Integer id = tryParseId(nameOrId);
        String cachedNameFr = (id != null) ? cacheService.getFrenchName(id) : null;

        if (cachedNameFr != null) {
            JsonNode pokemon = restTemplate.getForObject(POKEAPI_URL + "/pokemon/" + nameOrId, JsonNode.class);
            ((ObjectNode) pokemon).put("nameFr", cachedNameFr);
            return pokemon;
        }

        // Cache miss: fetch pokemon + species in parallel
        CompletableFuture<JsonNode> pokemonFuture = CompletableFuture.supplyAsync(() ->
                restTemplate.getForObject(POKEAPI_URL + "/pokemon/" + nameOrId, JsonNode.class));
        CompletableFuture<String> nameFrFuture = CompletableFuture.supplyAsync(() ->
                fetchFrenchName(nameOrId));

        JsonNode pokemon = pokemonFuture.join();
        String nameFr = nameFrFuture.join();

        if (id != null) cacheService.putFrenchName(id, nameFr);
        ((ObjectNode) pokemon).put("nameFr", nameFr);
        return pokemon;
    }

    public JsonNode searchPokemon(String query, int page, int size) {
        List<Integer> allIds = cacheService.search(query);
        int total = allIds.size();

        int from = page * size;
        List<Integer> pageIds = from < total
                ? allIds.subList(from, Math.min(from + size, total))
                : List.of();

        List<CompletableFuture<JsonNode>> futures = pageIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> getPokemonDetail(String.valueOf(id))))
                .collect(Collectors.toList());

        List<JsonNode> details = futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingInt(n -> n.get("id").asInt()))
                .collect(Collectors.toList());

        ObjectNode result = objectMapper.createObjectNode();
        result.put("count", total);
        ArrayNode array = objectMapper.createArrayNode();
        details.forEach(array::add);
        result.set("results", array);
        return result;
    }

    private String fetchFrenchName(String nameOrId) {
        try {
            JsonNode species = restTemplate.getForObject(
                    POKEAPI_URL + "/pokemon-species/" + nameOrId, JsonNode.class);
            if (species != null && species.has("names")) {
                for (JsonNode entry : species.get("names")) {
                    if ("fr".equals(entry.get("language").get("name").asText())) {
                        return entry.get("name").asText();
                    }
                }
            }
        } catch (Exception ignored) {}
        return nameOrId;
    }

    private Integer tryParseId(String nameOrId) {
        try { return Integer.parseInt(nameOrId); } catch (NumberFormatException e) { return null; }
    }
}