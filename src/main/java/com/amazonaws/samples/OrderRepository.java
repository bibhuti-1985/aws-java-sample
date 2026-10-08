/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * Persists {@link Order} records to a DynamoDB table.
 *
 * <p>Uses the AWS SDK for Java v2 ({@code software.amazon.awssdk.services.*}) client
 * builder pattern with {@code region()} configuration.
 */
public class OrderRepository {

    private static final Logger LOG = Logger.getLogger(OrderRepository.class.getName());

    private final DynamoDbClient dynamoClient;
    private final String tableName;
    private final SimpleDateFormat isoFormat;

    public OrderRepository(String tableName) {
        this.tableName = tableName;
        this.dynamoClient = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        // SimpleDateFormat is not thread-safe; a modern rewrite would use
        // java.time.format.DateTimeFormatter.ISO_INSTANT.
        this.isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    /**
     * Writes an order to the backing table. Overwrites any existing record
     * with the same {@code orderId}.
     *
     * @param order the order to persist; must not be null.
     */
    public void save(Order order) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("orderId", AttributeValue.builder().s(order.getOrderId()).build());
        item.put("customerId", AttributeValue.builder().s(order.getCustomerId()).build());
        item.put("currency", AttributeValue.builder().s(order.getCurrency()).build());
        item.put("status", AttributeValue.builder().s(order.getStatus().name()).build());
        item.put("total", AttributeValue.builder().n(order.getTotal().toPlainString()).build());
        item.put("createdAt", AttributeValue.builder().s(isoFormat.format(order.getCreatedAt())).build());
        item.put("updatedAt", AttributeValue.builder().s(isoFormat.format(order.getUpdatedAt())).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoClient.putItem(request);
        LOG.info("Saved order " + order.getOrderId() + " to table " + tableName);
    }

    /**
     * Loads a single order by id.
     *
     * @param orderId the primary key.
     * @return a sparse Order populated from the row, or {@code null} when absent.
     */
    public Order findById(String orderId) {
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put("orderId", AttributeValue.builder().s(orderId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse result = dynamoClient.getItem(request);
        if (result.item() == null || result.item().isEmpty()) {
            return null;
        }

        Map<String, AttributeValue> row = result.item();
        Order order = new Order();
        order.setOrderId(row.get("orderId").s());
        if (row.containsKey("customerId")) {
            order.setCustomerId(row.get("customerId").s());
        }
        if (row.containsKey("currency")) {
            order.setCurrency(row.get("currency").s());
        }
        if (row.containsKey("status")) {
            order.setStatus(OrderStatus.valueOf(row.get("status").s()));
        }
        return order;
    }
}
