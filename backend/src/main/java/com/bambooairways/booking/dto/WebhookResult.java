package com.bambooairways.booking.dto;

/**
 * WebhookResult
 */
public record WebhookResult(
    boolean isValidSignature,   // Chữ ký HMAC/RSA có hợp lệ không
    boolean isSuccess,          // Giao dịch thành công hay thất bại
    String orderCode,           // Mã đơn hàng/booking của hệ thống bạn
    String providerTransId,     // Mã giao dịch phía MoMo/VNPAY/Stripe (để lưu vết/đối soát)
    long amount,                // Số tiền thực tế khách đã thanh toán (đã chia 100 nếu là VNPAY)
    String rawResponse          // JSON gốc (tùy chọn, để lưu log DB)
) {

}
