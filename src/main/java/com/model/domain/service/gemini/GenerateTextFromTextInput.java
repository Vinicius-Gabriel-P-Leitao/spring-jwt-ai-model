/*
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * Licensed under the BSD 3-Clause License.
 * See LICENSE file in the project root for full license information.
 */
package com.model.domain.service.gemini;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GenerateTextFromTextInput {
    public void generateConnection() {
        GenerateContentResponse response;

        try (Client client = new Client()) {
            response = client.models.generateContent("gemini-2.5-flash", "Estou testando a sua API.", null);
        }

        System.out.println(response.text());
    }
}