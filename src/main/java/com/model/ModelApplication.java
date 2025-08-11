package com.model;

import com.model.core.config.VaultSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ModelApplication {
    public static void main(String[] args) {
//        GenerateTextFromTextInput generateTextFromTextInput = new GenerateTextFromTextInput();
//        generateTextFromTextInput.generateConnection();

        SpringApplication app = new SpringApplication(ModelApplication.class);
        app.addInitializers(new VaultSourceConfig());
        app.run();
    }
}
