/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Hypothetical order-processing service that orchestrates persistence,
 * invoicing, queue publishing, and customer notification.
 *
 * <p>This sample is intentionally written in a dated style to showcase
 * what AWS Transform's continuous modernization flow can address:
 *
 * <ol>
 *   <li><b>AWS SDK v1 &rarr; v2</b> &mdash; every service wrapper
 *       ({@link OrderRepository}, {@link OrderQueuePublisher},
 *       {@link InvoiceStorageService}, {@link OrderNotificationService})
 *       uses {@code com.amazonaws.services.*} clients.</li>
 *   <li><b>JDK language upgrade</b> &mdash; this file uses Joda-Time, an
 *       anonymous {@link Comparator} instead of a lambda, index-based
 *       {@code for} loops, and manual string concatenation. A modernized
 *       version would use {@code java.time}, lambdas, streams, and
 *       {@code String.join}.</li>
 *   <li><b>EOL / outdated dependencies</b> &mdash; the build pins
 *       {@code aws-java-sdk 1.9.6}, {@code joda-time 2.3}, and
 *       {@code commons-lang 2.6}, all of which a dependency scanner
 *       would flag.</li>
 * </ol>
 */
public class OrderProcessingService {

    private static final Logger LOG = Logger.getLogger(OrderProcessingService.class.getName());

    private final OrderRepository repository;
    private final OrderQueuePublisher queuePublisher;
    private final InvoiceStorageService invoiceStore;
    private final OrderNotificationService notifier;

    public OrderProcessingService(OrderRepository repository,
                                  OrderQueuePublisher queuePublisher,
                                  InvoiceStorageService invoiceStore,
                                  OrderNotificationService notifier) {
        this.repository = repository;
        this.queuePublisher = queuePublisher;
        this.invoiceStore = invoiceStore;
        this.notifier = notifier;
    }

    /**
     * Runs an order through the full pipeline: validate, persist, invoice,
     * publish, and notify.
     *
     * @param order the order to process.
     * @throws Exception if any downstream step fails. In a modern rewrite
     *     this would be a narrower, domain-specific exception.
     */
    public void processOrder(Order order) throws Exception {
        DateTime start = new DateTime();
        LOG.info("Processing order " + order.getOrderId() + " at " + start);

        if (!validate(order)) {
            order.setStatus(OrderStatus.FAILED);
            repository.save(order);
            LOG.warning("Order " + order.getOrderId() + " failed validation");
            return;
        }
        order.setStatus(OrderStatus.VALIDATED);

        repository.save(order);
        invoiceStore.uploadInvoice(order);

        order.setStatus(OrderStatus.PAID);
        repository.save(order);
        queuePublisher.publishStatusChange(order);
        notifier.notifyCustomer(order);

        DateTime end = new DateTime();
        Duration elapsed = new Duration(start, end);
        LOG.info("Finished processing order " + order.getOrderId()
                + " in " + elapsed.getMillis() + " ms");
    }

    /**
     * Returns the orders ranked by total, highest first.
     *
     * <p>Uses an anonymous {@link Comparator} rather than
     * {@code Comparator.comparing(...).reversed()} with a lambda.
     *
     * @param orders list of orders to rank; not modified.
     * @return a new list ordered by descending total.
     */
    public List<Order> rankByTotalDesc(List<Order> orders) {
        List<Order> copy = new ArrayList<Order>(orders);
        Collections.sort(copy, new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                return b.getTotal().compareTo(a.getTotal());
            }
        });
        return copy;
    }

    private boolean validate(Order order) {
        if (order == null) {
            return false;
        }
        if (StringUtils.isBlank(order.getOrderId())
                || StringUtils.isBlank(order.getCustomerId())) {
            return false;
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return false;
        }
        return order.getTotal().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Local smoke-test entry point. Wires up each collaborator with
     * placeholder resource names so the sample compiles and documents
     * the expected configuration surface. The AWS calls will of course
     * fail without real DynamoDB / SQS / S3 / SNS resources.
     *
     * @param args ignored.
     * @throws Exception when any pipeline step fails.
     */
    public static void main(String[] args) throws Exception {
        OrderRepository repo = new OrderRepository("orders-table");
        OrderQueuePublisher queue = new OrderQueuePublisher(
                "https://sqs.us-west-2.amazonaws.com/123456789012/orders-events");
        InvoiceStorageService invoices = new InvoiceStorageService("my-invoices-bucket");
        OrderNotificationService notifications = new OrderNotificationService(
                "arn:aws:sns:us-west-2:123456789012:order-notifications");

        OrderProcessingService service = new OrderProcessingService(
                repo, queue, invoices, notifications);

        Order order = new Order("ORD-1001", "CUST-42", "USD");
        order.addItem(new OrderItem("SKU-A", "Mechanical keyboard", 1, new BigDecimal("129.99")));
        order.addItem(new OrderItem("SKU-B", "USB-C cable", 2, new BigDecimal("9.49")));

        service.processOrder(order);
    }
}
