package com.retail.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisteredEvent {
    private Long customerId;
    private String email;
    private String firstName;
    private String lastName;
    private Instant registeredAt;
}

