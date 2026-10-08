/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.util.logging.Logger;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

/**
 * Fans customer-facing order notifications out through SNS.
 *
 * <p>Uses the AWS SDK v2 client builder pattern.
 */
public class OrderNotificationService {

    private static final Logger LOG = Logger.getLogger(OrderNotificationService.class.getName());

    private final SnsClient snsClient;
    private final String topicArn;

    public OrderNotificationService(String topicArn) {
        this.topicArn = topicArn;
        this.snsClient = SnsClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Publishes a human-readable notification about an order status change.
     *
     * @param order the order whose status just changed.
     * @return the SNS message id assigned by the service.
     */
    public String notifyCustomer(Order order) {
        String subject = "Order " + order.getOrderId() + " is now " + order.getStatus().name();
        String message = "Hi,\n\nYour order " + order.getOrderId()
                + " has moved to status " + order.getStatus().name()
                + ".\nTotal: " + order.getTotal().toPlainString() + " " + order.getCurrency()
                + "\n\nThanks for shopping with us.";

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build();

        PublishResponse result = snsClient.publish(request);
        LOG.info("Notified customer for order " + order.getOrderId()
                + " messageId=" + result.messageId());
        return result.messageId();
    }
}
