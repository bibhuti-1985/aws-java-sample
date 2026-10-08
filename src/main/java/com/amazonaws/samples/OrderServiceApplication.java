/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the order-processing service.
 *
 * <p>Pinned to Spring Boot 2.7.x to exercise AWS Transform's managed
 * Spring Boot 2.x &rarr; 3.x upgrade transformation.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
