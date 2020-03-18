package com.example.reactive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import java.net.URI;

@Configuration
public class DynamoConfig {
    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient(){
        return DynamoDbAsyncClient.builder()
                .region(Region.AP_EAST_1)
                .endpointOverride(URI.create("http://localhost:4569"))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }

    @Bean
    DbMigrator dbMigrator() {
        return new DbMigrator();
    };
}
