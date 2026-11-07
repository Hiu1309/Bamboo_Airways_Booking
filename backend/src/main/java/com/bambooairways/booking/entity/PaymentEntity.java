package com.bambooairways.booking.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class PaymentEntity {
    @Id public String id;
    public String bookingId;
    public String method;
    public String providerRef;
    public int amount;
    @Enumerated(EnumType.STRING) public PaymentStatus status = PaymentStatus.PENDING;
    public Instant paidAt;
    public Instant createdAt;
}
