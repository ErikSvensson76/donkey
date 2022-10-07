package com.example.donkey;

import com.example.donkey.property.DocumentStorageProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {DocumentStorageProperty.class})
public class DonkeyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DonkeyApplication.class, args);
    }

}
