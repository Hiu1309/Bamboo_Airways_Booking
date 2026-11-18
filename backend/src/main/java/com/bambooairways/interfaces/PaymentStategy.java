package com.bambooairways.interfaces;

import com.bambooairways.booking.dto.WebhookResult;
import com.bambooairways.enums.PaymentProvider;

public interface PaymentStategy {
    PaymentProvider getPaymentProvider();
    String createPaymentUrl(String orderId, double amount, String description);
    WebhookResult handleWebhook(String payload, String signature);
}
