/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the AWS-SDK-v1-based collaborators as Spring beans.
 *
 * <p>Resource names are externalized to {@code application.properties}
 * under the {@code app.orders.*} prefix.
 */
@Configuration
public class OrderServiceConfiguration {

    @Bean
    public OrderRepository orderRepository(
            @Value("${app.orders.table}") String tableName) {
        return new OrderRepository(tableName);
    }

    @Bean
    public OrderQueuePublisher orderQueuePublisher(
            @Value("${app.orders.queue-url}") String queueUrl) {
        return new OrderQueuePublisher(queueUrl);
    }

    @Bean
    public InvoiceStorageService invoiceStorageService(
            @Value("${app.orders.invoice-bucket}") String bucketName) {
        return new InvoiceStorageService(bucketName);
    }

    @Bean
    public OrderNotificationService orderNotificationService(
            @Value("${app.orders.notification-topic}") String topicArn) {
        return new OrderNotificationService(topicArn);
    }

    @Bean
    public OrderProcessingService orderProcessingService(
            OrderRepository repository,
            OrderQueuePublisher queuePublisher,
            InvoiceStorageService invoiceStore,
            OrderNotificationService notifier) {
        return new OrderProcessingService(
                repository, queuePublisher, invoiceStore, notifier);
    }
}
