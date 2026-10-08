/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Domain model for a customer order.
 *
 * <p>Uses {@link java.util.Date} for timestamps and a mutable item list.
 * A modernization pass would move timestamps to {@code java.time.Instant}
 * and convert this class into an immutable record or a Lombok {@code @Value}.
 */
public class Order {

    private String orderId;
    private String customerId;
    private String currency;
    private OrderStatus status;
    private Date createdAt;
    private Date updatedAt;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<OrderItem>();
        this.status = OrderStatus.NEW;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }

    public Order(String orderId, String customerId, String currency) {
        this();
        this.orderId = orderId;
        this.customerId = customerId;
        this.currency = currency;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() {
        // NOTE: returning the internal Date exposes mutable state. A modern
        // rewrite would return an immutable Instant.
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    /**
     * Sums the line totals of every item on this order.
     *
     * @return total price across all items, or {@code BigDecimal.ZERO} when empty.
     */
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        // old-school index loop; a modern rewrite would use
        // items.stream().map(OrderItem::getLineTotal).reduce(ZERO, BigDecimal::add)
        for (int i = 0; i < items.size(); i++) {
            total = total.add(items.get(i).getLineTotal());
        }
        return total;
    }
}
