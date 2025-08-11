/*
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * Licensed under the BSD 3-Clause License.
 * See LICENSE file in the project root for full license information.
 */
package com.model.core.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.core.error.ErrorCode;
import com.model.core.error.custom.VaultException;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class VaultSourceConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String VAULT_URI = "http://vault:8200/v1/secret/data/";
    private static final String VAULT_TOKEN_HEADER = "X-Vault-Token";
    private static final String VAULT_TOKEN_ENV = "ROOT_TOKEN";

    private final VaultHttpClient vaultHttpClient;

    public VaultSourceConfig() {
        this.vaultHttpClient = new VaultHttpClient();
    }

    public VaultSourceConfig(VaultHttpClient client) {
        this.vaultHttpClient = client;
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        try {
            String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
            String vaultToken = applicationContext.getEnvironment().getProperty(VAULT_TOKEN_ENV);

            InputStream inputStream = vaultHttpClient.getSecretsInputStream(appName, vaultToken);
            Map<String, Object> secrets = parseSecrets(inputStream);

            applicationContext.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("vault-secrets", secrets));
        } catch (Exception exception) {
            throw new VaultException(ErrorCode.VAULT_ERROR, "❌ Erro ao carregar secrets do Vault", exception);
        }
    }

    protected Map<String, Object> parseSecrets(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputStream);
        JsonNode dataNode = root.path("data").path("data");
        return mapper.convertValue(dataNode, new TypeReference<>() {
        });
    }

    public static class VaultHttpClient {
        public InputStream getSecretsInputStream(String appName, String vaultToken) throws IOException, URISyntaxException {
            if (vaultToken == null || vaultToken.isEmpty()) {
                throw new VaultException(ErrorCode.VAULT_ERROR, "Token do Vault não definido em " + VAULT_TOKEN_ENV);
            }

            HttpURLConnection connection = (HttpURLConnection) new URI(VAULT_URI + appName).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty(VAULT_TOKEN_HEADER, vaultToken);

            if (connection.getResponseCode() != 200) {
                throw new VaultException(ErrorCode.VAULT_ERROR,
                        "Erro ao acessar Vault: HTTP " + connection.getResponseCode());
            }

            return connection.getInputStream();
        }
    }
}