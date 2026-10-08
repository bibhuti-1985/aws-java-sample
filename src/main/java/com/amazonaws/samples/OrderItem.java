/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.math.BigDecimal;

/**
 * A single line item on an order.
 *
 * <p>Classic mutable Java-7 style POJO with explicit getters and setters.
 * In modern Java (14+) this would typically be a {@code record}.
 */
public class OrderItem {

    private String sku;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItem() {
        // required for frameworks that instantiate reflectively
    }

    public OrderItem(String sku, String description, int quantity, BigDecimal unitPrice) {
        this.sku = sku;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * @return quantity * unitPrice, rounded to the scale of unitPrice.
     */
    public BigDecimal getLineTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}
