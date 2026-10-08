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

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

/**
 * Persists {@link Order} records to a DynamoDB table.
 *
 * <p>Uses the AWS SDK for Java v1 ({@code com.amazonaws.services.*}) client
 * constructor pattern and the {@code setRegion(Region)} setter. A v2 rewrite
 * would use {@code DynamoDbClient.builder().region(...).build()} and
 * {@code software.amazon.awssdk.*} packages.
 */
public class OrderRepository {

    private static final Logger LOG = Logger.getLogger(OrderRepository.class.getName());

    private final AmazonDynamoDBClient dynamoClient;
    private final String tableName;
    private final SimpleDateFormat isoFormat;

    public OrderRepository(String tableName) {
        this.tableName = tableName;
        // v1 pattern: construct the client, then set the region via a setter.
        this.dynamoClient = new AmazonDynamoDBClient(new DefaultAWSCredentialsProviderChain());
        this.dynamoClient.setRegion(Region.getRegion(Regions.US_WEST_2));
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
        item.put("orderId", new AttributeValue(order.getOrderId()));
        item.put("customerId", new AttributeValue(order.getCustomerId()));
        item.put("currency", new AttributeValue(order.getCurrency()));
        item.put("status", new AttributeValue(order.getStatus().name()));
        item.put("total", new AttributeValue().withN(order.getTotal().toPlainString()));
        item.put("createdAt", new AttributeValue(isoFormat.format(order.getCreatedAt())));
        item.put("updatedAt", new AttributeValue(isoFormat.format(order.getUpdatedAt())));

        PutItemRequest request = new PutItemRequest()
                .withTableName(tableName)
                .withItem(item);

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
        key.put("orderId", new AttributeValue(orderId));

        GetItemRequest request = new GetItemRequest()
                .withTableName(tableName)
                .withKey(key);

        GetItemResult result = dynamoClient.getItem(request);
        if (result.getItem() == null || result.getItem().isEmpty()) {
            return null;
        }

        Map<String, AttributeValue> row = result.getItem();
        Order order = new Order();
        order.setOrderId(row.get("orderId").getS());
        if (row.containsKey("customerId")) {
            order.setCustomerId(row.get("customerId").getS());
        }
        if (row.containsKey("currency")) {
            order.setCurrency(row.get("currency").getS());
        }
        if (row.containsKey("status")) {
            order.setStatus(OrderStatus.valueOf(row.get("status").getS()));
        }
        return order;
    }
}
