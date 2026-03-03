package com.keycloak.starter.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final Keycloak keycloakAdminClient;

    @Value("${keycloak.realm}")
    private String realm;


    public String createUser(String username, String email,
                             String firstName, String lastName,
                             String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = getRealmResource().users();
        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                String location = response.getHeaderString("Location");
                String userId = location.substring(location.lastIndexOf("/") + 1);
                log.info("Created user: {} with ID: {}", username, userId);
                return userId;
            } else {
                String error = response.readEntity(String.class);
                throw new RuntimeException("Failed to create user: " + error);
            }
        }
    }


    public void deleteUser(String userId) {
        getRealmResource().users().get(userId).remove();
        log.info("Deleted user with ID: {}", userId);
    }


    public List<UserRepresentation> getAllUsers() {
        return getRealmResource().users().list();
    }


    public List<UserRepresentation> searchUsers(String search) {
        return getRealmResource().users().search(search);
    }


    public void resetPassword(String userId, String newPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);
        getRealmResource().users().get(userId).resetPassword(credential);
        log.info("Reset password for user ID: {}", userId);
    }


    public void setUserEnabled(String userId, boolean enabled) {
        UserRepresentation user = getRealmResource().users().get(userId).toRepresentation();
        user.setEnabled(enabled);
        getRealmResource().users().get(userId).update(user);
        log.info("Set user {} enabled={}", userId, enabled);
    }


    public void assignRole(String userId, String roleName) {
        RoleRepresentation role = getRealmResource().roles().get(roleName).toRepresentation();
        getRealmResource().users().get(userId).roles()
            .realmLevel().add(Collections.singletonList(role));
        log.info("Assigned role {} to user {}", roleName, userId);
    }


    public void revokeRole(String userId, String roleName) {
        RoleRepresentation role = getRealmResource().roles().get(roleName).toRepresentation();
        getRealmResource().users().get(userId).roles()
            .realmLevel().remove(Collections.singletonList(role));
        log.info("Revoked role {} from user {}", roleName, userId);
    }


    public List<RoleRepresentation> getUserRoles(String userId) {
        return getRealmResource().users().get(userId).roles().realmLevel().listEffective();
    }


    private RealmResource getRealmResource() {
        return keycloakAdminClient.realm(realm);
    }
}
