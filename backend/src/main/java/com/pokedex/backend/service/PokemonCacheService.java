package com.pokedex.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PokemonCacheService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String POKEAPI_URL = "https://pokeapi.co/api/v2";
    private static final String CACHE_FILE = "pokemon-names-cache.json";

    public record PokemonRef(int id, String nameEn) {}

    private volatile List<PokemonRef> pokemonIndex = Collections.emptyList();
    private final ConcurrentHashMap<Integer, String> frenchNames = new ConcurrentHashMap<>();
    private volatile boolean ready = false;

    @PostConstruct
    public void buildIndex() {
        Thread.ofVirtual().start(() -> {
            try {
                log.info("Building Pokémon index...");

                // Toujours charger la liste complète (un seul appel, rapide)
                JsonNode response = restTemplate.getForObject(
                        POKEAPI_URL + "/pokemon?limit=10000", JsonNode.class);
                if (response == null || !response.has("results")) return;

                List<PokemonRef> refs = new ArrayList<>();
                for (JsonNode item : response.get("results")) {
                    String url = item.get("url").asText();
                    String[] parts = url.split("/");
                    int id = Integer.parseInt(parts[parts.length - 1]);
                    refs.add(new PokemonRef(id, item.get("name").asText()));
                }
                pokemonIndex = Collections.unmodifiableList(refs);
                log.info("Loaded {} Pokémon.", refs.size());

                // Charger les noms français depuis le cache JSON si disponible
                if (loadCacheFromFile()) {
                    log.info("French names loaded from cache ({} entries).", frenchNames.size());
                    ready = true;
                    return;
                }

                // Première exécution : appel PokéAPI pour tous les noms français
                log.info("No cache found, fetching French names from PokéAPI (one-time operation)...");
                ExecutorService executor = Executors.newFixedThreadPool(20);
                for (PokemonRef ref : refs) {
                    executor.submit(() -> fetchAndCacheName(ref.id()));
                }
                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.MINUTES);

                saveCacheToFile();
                ready = true;
                log.info("Pokémon index ready — {} French names cached and saved to {}.",
                        frenchNames.size(), CACHE_FILE);

            } catch (Exception e) {
                log.error("Failed to build Pokémon index", e);
            }
        });
    }

    private boolean loadCacheFromFile() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) return false;
        try {
            Map<Integer, String> loaded = objectMapper.readValue(
                    file, new TypeReference<Map<Integer, String>>() {});
            frenchNames.putAll(loaded);
            return !frenchNames.isEmpty();
        } catch (Exception e) {
            log.warn("Failed to read cache file '{}': {}", CACHE_FILE, e.getMessage());
            return false;
        }
    }

    private void saveCacheToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(CACHE_FILE), frenchNames);
            log.info("Cache saved to '{}'.", CACHE_FILE);
        } catch (Exception e) {
            log.warn("Failed to save cache file '{}': {}", CACHE_FILE, e.getMessage());
        }
    }

    private void fetchAndCacheName(int id) {
        try {
            JsonNode species = restTemplate.getForObject(
                    POKEAPI_URL + "/pokemon-species/" + id, JsonNode.class);
            if (species == null || !species.has("names")) return;
            for (JsonNode entry : species.get("names")) {
                if ("fr".equals(entry.get("language").get("name").asText())) {
                    frenchNames.put(id, entry.get("name").asText());
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    public boolean isReady() { return ready; }
    public boolean hasFrenchName(int id) { return frenchNames.containsKey(id); }
    public String getFrenchName(int id) { return frenchNames.get(id); }
    public void putFrenchName(int id, String name) { frenchNames.put(id, name); }

    public List<Integer> search(String query) {
        String q = normalize(query);
        List<Integer> results = new ArrayList<>();
        for (PokemonRef ref : pokemonIndex) {
            String fr = normalize(frenchNames.getOrDefault(ref.id(), ""));
            if (fr.startsWith(q)) {
                results.add(ref.id());
            }
        }
        return results;
    }

    private String normalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.toLowerCase(Locale.ROOT)
                .replace("é", "e").replace("è", "e").replace("ê", "e").replace("ë", "e")
                .replace("à", "a").replace("â", "a").replace("ä", "a")
                .replace("ï", "i").replace("î", "i")
                .replace("ô", "o").replace("ö", "o")
                .replace("û", "u").replace("ü", "u")
                .replace("ç", "c");
    }
}