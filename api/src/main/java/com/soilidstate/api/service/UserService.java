package com.soilidstate.api.service;

import com.soilidstate.api.entity.User;
import com.soilidstate.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get or create user from OAuth2 JWT token
     */
    @Transactional
    public User getOrCreateUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String oauthSubject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String issuer = jwt.getIssuer().toString();

        // Determine provider from issuer
        String provider = determineProvider(issuer);

        return userRepository.findByOauthSubject(oauthSubject)
                .orElseGet(() -> createUser(oauthSubject, email, name, provider));
    }

    /**
     * Get current user from authentication
     */
    public User getCurrentUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String oauthSubject = jwt.getSubject();

        return userRepository.findByOauthSubject(oauthSubject)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private User createUser(String oauthSubject, String email, String name, String provider) {
        log.info("Creating new user: {} from provider: {}", email, provider);

        User user = User.builder()
                .oauthSubject(oauthSubject)
                .email(email)
                .displayName(name)
                .oauthProvider(provider)
                .build();

        return userRepository.save(user);
    }

    private String determineProvider(String issuer) {
        if (issuer.contains("auth0.com")) {
            return "auth0";
        } else if (issuer.contains("google.com") || issuer.contains("accounts.google.com")) {
            return "google";
        } else if (issuer.contains("okta.com")) {
            return "okta";
        } else if (issuer.contains("microsoft.com") || issuer.contains("login.microsoftonline.com")) {
            return "microsoft";
        } else {
            return "unknown";
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
