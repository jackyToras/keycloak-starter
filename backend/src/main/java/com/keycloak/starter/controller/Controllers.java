package com.keycloak.starter.controller;

import com.keycloak.starter.service.KeycloakAdminService;
import com.keycloak.starter.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/public")
class PublicController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Keycloak Starter is running"
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
            "app", "Keycloak Starter Kit",
            "version", "1.0.0",
            "description", "Plug-and-play Keycloak + Spring Boot security starter"
        ));
    }
}


@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
            "userId",    jwt.getSubject(),
            "username",  jwt.getClaimAsString("preferred_username"),
            "email",     jwt.getClaimAsString("email"),
            "firstName", jwt.getClaimAsString("given_name"),
            "lastName",  jwt.getClaimAsString("family_name"),
            "roles",     jwt.getClaimAsMap("realm_access")
        ));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> dashboard(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome " + jwt.getClaimAsString("preferred_username"),
            "access",  "User level"
        ));
    }
}


@RestController
@RequestMapping("/api/manager")
class ManagerController {

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> reports() {
        return ResponseEntity.ok(Map.of(
            "message", "Manager reports access granted",
            "data",    List.of("Report Q1", "Report Q2", "Report Q3")
        ));
    }
}


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
class AdminController {

    private final KeycloakAdminService keycloakAdminService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserRepresentation>> getAllUsers() {
        return ResponseEntity.ok(keycloakAdminService.getAllUsers());
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody Map<String, String> req) {
        String userId = keycloakAdminService.createUser(
            req.get("username"),
            req.get("email"),
            req.get("firstName"),
            req.get("lastName"),
            req.get("password")
        );
        return ResponseEntity.ok(Map.of("userId", userId, "message", "User created successfully"));
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        keycloakAdminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PostMapping("/users/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> assignRole(
        @PathVariable String userId,
        @PathVariable String roleName) {
        keycloakAdminService.assignRole(userId, roleName);
        return ResponseEntity.ok(Map.of("message", "Role " + roleName + " assigned"));
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> revokeRole(
        @PathVariable String userId,
        @PathVariable String roleName) {
        keycloakAdminService.revokeRole(userId, roleName);
        return ResponseEntity.ok(Map.of("message", "Role " + roleName + " revoked"));
    }

    @PostMapping("/users/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(
        @PathVariable String userId,
        @RequestBody Map<String, String> req) {
        keycloakAdminService.resetPassword(userId, req.get("password"));
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}


@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
class TokenController {

    private final TokenService tokenService;

    @GetMapping("/client-credentials")
    public ResponseEntity<Map<String, Object>> clientCredentials() {
        return ResponseEntity.ok(tokenService.getClientCredentialsToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> req) {
        return ResponseEntity.ok(tokenService.refreshAccessToken(req.get("refreshToken")));
    }

    @PostMapping("/introspect")
    public ResponseEntity<Map<String, Object>> introspect(@RequestBody Map<String, String> req) {
        return ResponseEntity.ok(tokenService.introspectToken(req.get("token")));
    }
}
