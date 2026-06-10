package com.pokedex.backend.service;

import com.pokedex.backend.dto.UserResponse;
import com.pokedex.backend.entity.User;
import com.pokedex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getMe(String username) {
        User user = findUser(username);
        return toResponse(user);
    }

    public UserResponse setFavorite(String username, Long pokemonId) {
        User user = findUser(username);
        user.setFavoritePokemonId(pokemonId);
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse removeFavorite(String username) {
        User user = findUser(username);
        user.setFavoritePokemonId(null);
        userRepository.save(user);
        return toResponse(user);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFavoritePokemonId());
    }
}
