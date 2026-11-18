package com.bambooairways.booking.dto.payment;

import com.bambooairways.enums.PaymentProvider;

public record PaymentResponse(
    String transactionCode,
    PaymentProvider provider,
    String actionType, // "REDIRECT", "QR_CODE", "CLIENT_SECRET"
    String payUrl,
    String qrCodeUrl,
    String expiresAt
) {
    public static PaymentResponse redirect(String transactionCode, PaymentProvider provider, String payUrl) {
        return new PaymentResponse(transactionCode, provider, "REDIRECT", payUrl, null, null);
    }
}
