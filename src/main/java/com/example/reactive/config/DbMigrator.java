package com.example.reactive.config;
import com.example.reactive.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.annotation.PostConstruct;

public class DbMigrator {
    @Autowired
    private DynamoDbAsyncClient asyncClient;

    @PostConstruct
    public void migrate() {
        DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .build();

        asyncClient.describeTable(describeTableRequest).whenComplete(((describeTableResponse, err) -> {
            if(describeTableResponse == null) {
                GlobalSecondaryIndex bySoftKeyIndex = GlobalSecondaryIndex.builder()
                        .indexName(AppConstants.GSI_BY_SOFTKEY)
                        .provisionedThroughput(ProvisionedThroughput.builder()
                                .readCapacityUnits((long) 1)
                                .writeCapacityUnits((long) 1)
                                .build())
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .keySchema(
                                KeySchemaElement.builder()
                                        .attributeName(AppConstants.SOFT_KEY)
                                        .keyType(KeyType.HASH).build(),
                                KeySchemaElement.builder()
                                        .attributeName(AppConstants.PARTITION_KEY)
                                        .keyType(KeyType.RANGE).build()
                        ).build();

                GlobalSecondaryIndex byPopularityIndex = GlobalSecondaryIndex.builder()
                        .indexName(AppConstants.GSI_BY_POPULARITY)
                        .provisionedThroughput(ProvisionedThroughput.builder()
                                .readCapacityUnits((long) 1)
                                .writeCapacityUnits((long) 1)
                                .build())
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .keySchema(
                                KeySchemaElement.builder()
                                        .attributeName("entity_name")
                                        .keyType(KeyType.HASH).build(),
                                KeySchemaElement.builder()
                                        .attributeName(AppConstants.POPULARITY)
                                        .keyType(KeyType.RANGE).build()
                        ).build();

                GlobalSecondaryIndex byEntityNameIndex = GlobalSecondaryIndex.builder()
                        .indexName(AppConstants.GSI_BY_ENTITY_NAME)
                        .provisionedThroughput(ProvisionedThroughput.builder()
                                .readCapacityUnits((long) 1)
                                .writeCapacityUnits((long) 1)
                                .build())
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .keySchema(
                                KeySchemaElement.builder()
                                        .attributeName("entity_name")
                                        .keyType(KeyType.HASH).build(),
                                KeySchemaElement.builder()
                                        .attributeName(AppConstants.SOFT_KEY)
                                        .keyType(KeyType.RANGE).build()
                        ).build();

                GlobalSecondaryIndex byTimeIndex = GlobalSecondaryIndex.builder()
                        .indexName(AppConstants.GSI_BY_TIME)
                        .provisionedThroughput(ProvisionedThroughput.builder()
                                .readCapacityUnits((long) 1)
                                .writeCapacityUnits((long) 1)
                                .build())
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .keySchema(
                                KeySchemaElement.builder()
                                        .attributeName("entity_name")
                                        .keyType(KeyType.HASH).build(),
                                KeySchemaElement.builder()
                                        .attributeName(AppConstants.UPDATED_AT)
                                        .keyType(KeyType.RANGE).build()
                        ).build();

                CreateTableRequest createTableRequest = CreateTableRequest.builder()
                        .attributeDefinitions(
                                AttributeDefinition.builder()
                                        .attributeName(AppConstants.PARTITION_KEY)
                                        .attributeType(ScalarAttributeType.S).build(),
                                AttributeDefinition.builder()
                                        .attributeName(AppConstants.SOFT_KEY)
                                        .attributeType(ScalarAttributeType.S).build(),
                                AttributeDefinition.builder()
                                        .attributeName("entity_name")
                                        .attributeType(ScalarAttributeType.S).build(),
                                AttributeDefinition.builder()
                                        .attributeName(AppConstants.POPULARITY)
                                        .attributeType(ScalarAttributeType.N).build(),
                                AttributeDefinition.builder()
                                        .attributeName(AppConstants.UPDATED_AT)
                                        .attributeType(ScalarAttributeType.S).build()
                        )
                        .keySchema(
                                KeySchemaElement.builder()
                                        .attributeName(AppConstants.PARTITION_KEY)
                                        .keyType(KeyType.HASH).build(),
                                KeySchemaElement.builder()
                                        .attributeName(AppConstants.SOFT_KEY)
                                        .keyType(KeyType.RANGE).build()

                        )
                        .provisionedThroughput(ProvisionedThroughput.builder()
                                .readCapacityUnits((long) 1)
                                .writeCapacityUnits((long) 1)
                                .build())
                        .tableName(AppConstants.TABLE_NAME)
                        .globalSecondaryIndexes(bySoftKeyIndex, byPopularityIndex, byTimeIndex, byEntityNameIndex)
                        .build();

                asyncClient.createTable(createTableRequest).thenApply((createTableResponse) -> {
                    System.out.println(createTableResponse);
                    return null;
                }).exceptionally(error -> {
                    System.out.print(String.valueOf(error));
                    return null;
                });
            }
            else {
                System.out.println(describeTableResponse);
            }
        }));
    }
}
