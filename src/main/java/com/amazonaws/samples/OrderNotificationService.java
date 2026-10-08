/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.util.logging.Logger;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * Fans customer-facing order notifications out through SNS.
 *
 * <p>Uses the AWS SDK v1 client-constructor + {@code setRegion} pattern.
 */
public class OrderNotificationService {

    private static final Logger LOG = Logger.getLogger(OrderNotificationService.class.getName());

    private final AmazonSNSClient snsClient;
    private final String topicArn;

    public OrderNotificationService(String topicArn) {
        this.topicArn = topicArn;
        this.snsClient = new AmazonSNSClient(new DefaultAWSCredentialsProviderChain());
        this.snsClient.setRegion(Region.getRegion(Regions.US_WEST_2));
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

        PublishRequest request = new PublishRequest()
                .withTopicArn(topicArn)
                .withSubject(subject)
                .withMessage(message);

        PublishResult result = snsClient.publish(request);
        LOG.info("Notified customer for order " + order.getOrderId()
                + " messageId=" + result.getMessageId());
        return result.getMessageId();
    }
}
