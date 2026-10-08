/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * Publishes order lifecycle events to a downstream SQS queue so that
 * fulfillment, billing, and analytics can consume them asynchronously.
 *
 * <p>Written against the AWS SDK for Java v2.
 */
public class OrderQueuePublisher {

    private static final Logger LOG = Logger.getLogger(OrderQueuePublisher.class.getName());

    private final SqsClient sqsClient;
    private final String queueUrl;

    public OrderQueuePublisher(String queueUrl) {
        if (StringUtils.isBlank(queueUrl)) {
            // commons-lang 2.x; superseded by commons-lang3 (org.apache.commons.lang3)
            throw new IllegalArgumentException("queueUrl must not be blank");
        }
        this.queueUrl = queueUrl;
        this.sqsClient = SqsClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Publishes a status-change event for the given order.
     *
     * @param order order whose status just changed.
     * @return the SQS message id assigned by the service.
     */
    public String publishStatusChange(Order order) {
        String body = buildMessageBody(order);
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build();

        try {
            SendMessageResponse result = sqsClient.sendMessage(request);
            LOG.info("Published status change for order " + order.getOrderId()
                    + " messageId=" + result.messageId());
            return result.messageId();
        } catch (Exception ex) {
            // broad catch; a modern rewrite would narrow this and wrap in a
            // domain-specific exception.
            LOG.log(Level.SEVERE,
                    "Failed to publish status change for order " + order.getOrderId(), ex);
            throw new RuntimeException(ex);
        }
    }

    private String buildMessageBody(Order order) {
        // naive JSON builder with manual string concatenation; a modern rewrite
        // would use Jackson, Gson, or the built-in java.util.json facilities.
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"orderId\":\"").append(order.getOrderId()).append("\",");
        sb.append("\"customerId\":\"").append(order.getCustomerId()).append("\",");
        sb.append("\"status\":\"").append(order.getStatus().name()).append("\",");
        sb.append("\"total\":").append(order.getTotal().toPlainString()).append(",");
        sb.append("\"currency\":\"").append(order.getCurrency()).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
