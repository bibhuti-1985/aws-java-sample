/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

/**
 * Renders a plain-text invoice for an order and uploads it to S3.
 *
 * <p>Illustrates older Java idioms that AWS Transform's language-upgrade
 * flow would modernize:
 * <ul>
 *   <li>pre-NIO {@code java.io.File} / {@code FileOutputStream}</li>
 *   <li>no try-with-resources — explicit {@code finally} close</li>
 *   <li>{@code SimpleDateFormat} instead of {@code DateTimeFormatter}</li>
 * </ul>
 */
public class InvoiceStorageService {

    private static final Logger LOG = Logger.getLogger(InvoiceStorageService.class.getName());

    private final S3Client s3Client;
    private final String bucketName;

    public InvoiceStorageService(String bucketName) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Renders a text invoice for the given order and uploads it to S3.
     *
     * @param order order to render.
     * @return the full {@code s3://bucket/key} URI of the uploaded invoice.
     * @throws IOException if the temp file cannot be written.
     */
    public String uploadInvoice(Order order) throws IOException {
        File invoiceFile = renderInvoice(order);
        String key = "invoices/" + order.getCustomerId() + "/" + order.getOrderId() + ".txt";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromFile(invoiceFile));

        LOG.info("Uploaded invoice for order " + order.getOrderId()
                + " to s3://" + bucketName + "/" + key);
        return "s3://" + bucketName + "/" + key;
    }

    private File renderInvoice(Order order) throws IOException {
        File file = File.createTempFile("invoice-" + order.getOrderId() + "-", ".txt");
        file.deleteOnExit();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Explicit resource management without try-with-resources.
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write("INVOICE\n");
            writer.write("================================\n");
            writer.write("Order ID:    " + order.getOrderId() + "\n");
            writer.write("Customer ID: " + order.getCustomerId() + "\n");
            writer.write("Issued at:   " + dateFormat.format(order.getCreatedAt()) + "\n");
            writer.write("Currency:    " + order.getCurrency() + "\n");
            writer.write("--------------------------------\n");
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItem item = order.getItems().get(i);
                writer.write(item.getQuantity() + " x " + item.getDescription()
                        + " @ " + item.getUnitPrice().toPlainString()
                        + " = " + item.getLineTotal().toPlainString() + "\n");
            }
            writer.write("--------------------------------\n");
            writer.write("TOTAL: " + order.getTotal().toPlainString() + " " + order.getCurrency() + "\n");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException closeEx) {
                    // swallow — matches older pre-try-with-resources patterns
                    LOG.log(Level.WARNING, "Failed to close invoice writer", closeEx);
                }
            }
        }
        return file;
    }
}
