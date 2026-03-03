package com.keycloak.starter.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.admin-realm}")
    private String adminRealm;

    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakUrl)
            .realm(adminRealm)
            .clientId(adminClientId)
            .username(adminUsername)
            .password(adminPassword)
            .grantType(OAuth2Constants.PASSWORD)
            .build();
    }
}
