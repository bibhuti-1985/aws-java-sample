/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST surface for the order-processing service.
 *
 * <p>Uses Spring MVC annotations from Spring Boot 2.7.x; a 3.x upgrade
 * would swap {@code javax.servlet} dependencies for {@code jakarta.servlet},
 * which is one of the mechanical changes AWS Transform's Spring Boot
 * upgrade handles.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProcessingService service;
    private final OrderRepository repository;

    @Autowired
    public OrderController(OrderProcessingService service,
                           OrderRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Order> submitOrder(@RequestBody Order order) throws Exception {
        service.processOrder(order);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        Order order = repository.findById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
}
