
/*
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * Licensed under the BSD 3-Clause License.
 * See LICENSE file in the project root for full license information.
 */
package com.model.core.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VaultSourceConfigTest {
    @Test
    void testInitialize() throws Exception {
        String fakeJson = "{\"data\":{\"data\":{\"POSTGRES_USER\":\"vinicius\"}}}";
        InputStream fakeStream = new ByteArrayInputStream(fakeJson.getBytes(StandardCharsets.UTF_8));

        VaultSourceConfig.VaultHttpClient mockClient = Mockito.mock(VaultSourceConfig.VaultHttpClient.class);
        Mockito.when(mockClient.getSecretsInputStream(Mockito.anyString(), Mockito.anyString())).thenReturn(fakeStream);

        VaultSourceConfig initializer = new VaultSourceConfig(mockClient);

        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment env = new StandardEnvironment();
        Mockito.when(context.getEnvironment()).thenReturn(env);

        initializer.initialize(context);

        assertEquals("vinicius", env.getProperty("POSTGRES_USER"));
    }
}