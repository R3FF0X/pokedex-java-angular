package com.pokedex.backend.controller;

import com.pokedex.backend.dto.UserResponse;
import com.pokedex.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Principal principal) {
        return ResponseEntity.ok(userService.getMe(principal.getName()));
    }

    @PutMapping("/favorite/{pokemonId}")
    public ResponseEntity<UserResponse> setFavorite(Principal principal, @PathVariable Long pokemonId) {
        return ResponseEntity.ok(userService.setFavorite(principal.getName(), pokemonId));
    }

    @DeleteMapping("/favorite")
    public ResponseEntity<UserResponse> removeFavorite(Principal principal) {
        return ResponseEntity.ok(userService.removeFavorite(principal.getName()));
    }
}