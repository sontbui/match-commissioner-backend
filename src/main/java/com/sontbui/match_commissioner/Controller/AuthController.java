package com.sontbui.match_commissioner.Controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sontbui.match_commissioner.Utils.JwtUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        // TODO: check username/password trong DB
        String accessToken = jwtUtils.generateToken(username, Map.of("role", "ADMIN"));
        String refreshToken = jwtUtils.generateRefreshToken(username);
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    @GetMapping("/me")
    public Map<String, String> me(@RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        return Map.of("user", jwtUtils.extractUsername(token));
    }
}

