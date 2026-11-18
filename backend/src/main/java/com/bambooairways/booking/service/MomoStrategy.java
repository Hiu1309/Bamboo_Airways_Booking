package com.bambooairways.booking.service;

import com.bambooairways.booking.dto.WebhookResult;
import com.bambooairways.enums.PaymentProvider;
import com.bambooairways.interfaces.PaymentStategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Component
public class MomoStrategy implements PaymentStategy {
    private final ObjectMapper json = new ObjectMapper();
    @Value("${MOMO_ENDPOINT:https://test-payment.momo.vn/v2/gateway/api/create}") private String endpoint;
    @Value("${MOMO_PARTNER_CODE:MOMO}") private String partner;
    @Value("${MOMO_ACCESS_KEY:}") private String access;
    @Value("${MOMO_SECRET_KEY:}") private String secret;
    @Value("${MOMO_IPN_URL:}") private String ipn;
    @Value("${FRONTEND_URL:http://localhost:3000}") private String frontend;

    @PostConstruct
    void normalizeConfig() {
        endpoint = endpoint.trim();
        partner = partner.trim();
        access = access.trim();
        secret = secret.trim();
        ipn = ipn.trim();
        frontend = frontend.trim();
    }

    @Override public PaymentProvider getPaymentProvider() { return PaymentProvider.MOMO; }

    @Override public String createPaymentUrl(String orderId, double amount, String description) {
        try {
            String requestId = orderId + "-" + System.currentTimeMillis();
            String orderInfo = description == null ? "Bamboo Airways booking" : description;
            String redirect = frontend + "/checkout/result";
            String raw = "accessKey=" + access + "&amount=" + Math.round(amount) + "&extraData=&ipnUrl=" + ipn
                    + "&orderId=" + orderId + "&orderInfo=" + orderInfo + "&partnerCode=" + partner
                    + "&redirectUrl=" + redirect + "&requestId=" + requestId + "&requestType=captureWallet";
            String signature = hmac(raw);
            JsonNode response = RestClient.create().post().uri(endpoint).body(Map.ofEntries(
                    Map.entry("partnerCode", partner),
                    Map.entry("requestId", requestId),
                    Map.entry("amount", Math.round(amount)),
                    Map.entry("orderId", orderId),
                    Map.entry("orderInfo", orderInfo),
                    Map.entry("redirectUrl", redirect),
                    Map.entry("ipnUrl", ipn),
                    Map.entry("requestType", "captureWallet"),
                    Map.entry("extraData", ""),
                    Map.entry("lang", "en"),
                    Map.entry("signature", signature)))
                    .retrieve().body(JsonNode.class);
            if (response == null || !response.hasNonNull("payUrl")) throw new IllegalStateException("MoMo response did not include payUrl");
            return response.get("payUrl").asText();
        } catch (Exception e) { throw new IllegalStateException("Could not create MoMo payment", e); }
    }

    @Override public WebhookResult handleWebhook(String payload, String signature) {
        try {
            JsonNode n = json.readTree(payload);
            String raw = "accessKey=" + access + "&amount=" + n.path("amount").asText() + "&extraData=" + n.path("extraData").asText("")
                    + "&message=" + n.path("message").asText("") + "&orderId=" + n.path("orderId").asText()
                    + "&orderInfo=" + n.path("orderInfo").asText("") + "&orderType=" + n.path("orderType").asText("")
                    + "&partnerCode=" + n.path("partnerCode").asText() + "&payType=" + n.path("payType").asText("")
                    + "&requestId=" + n.path("requestId").asText() + "&responseTime=" + n.path("responseTime").asText()
                    + "&resultCode=" + n.path("resultCode").asText() + "&transId=" + n.path("transId").asText();
            boolean valid = signature != null && MessageDigest.isEqual(hmac(raw).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
            return new WebhookResult(valid, valid && n.path("resultCode").asInt(-1) == 0, n.path("orderId").asText(), n.path("transId").asText(), n.path("amount").asLong(), payload);
        } catch (Exception e) { return new WebhookResult(false, false, null, null, 0, payload); }
    }
    private String hmac(String data) throws Exception { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))); }
}
