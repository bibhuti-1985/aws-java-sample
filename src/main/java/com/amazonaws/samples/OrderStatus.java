/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 */
package com.amazonaws.samples;

/**
 * Lifecycle status of an {@link Order}.
 */
public enum OrderStatus {
    NEW,
    VALIDATED,
    PAID,
    FULFILLED,
    CANCELLED,
    FAILED
}
