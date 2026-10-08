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

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

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

    private final AmazonS3 s3Client;
    private final String bucketName;

    public InvoiceStorageService(String bucketName) {
        this.bucketName = bucketName;
        this.s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
        this.s3Client.setRegion(Region.getRegion(Regions.US_WEST_2));
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

        PutObjectRequest request = new PutObjectRequest(bucketName, key, invoiceFile);
        s3Client.putObject(request);

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
