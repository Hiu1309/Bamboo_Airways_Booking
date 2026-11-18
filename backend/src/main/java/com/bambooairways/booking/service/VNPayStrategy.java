package com.bambooairways.booking.service;

import com.bambooairways.booking.dto.WebhookResult;
import com.bambooairways.enums.PaymentProvider;
import com.bambooairways.interfaces.PaymentStategy;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

@Component
public class VNPayStrategy implements PaymentStategy {
    @Value("${VNPAY_ENDPOINT:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") private String endpoint;
    @Value("${VNPAY_TMN_CODE:}") private String tmn;
    @Value("${VNPAY_HASH_SECRET:}") private String secret;
    @Value("${VNPAY_RETURN_URL:http://localhost:3000/checkout/result}") private String returnUrl;

    @PostConstruct
    void normalizeConfig() {
        endpoint = endpoint.trim();
        tmn = tmn.trim();
        secret = secret.trim();
        returnUrl = returnUrl.trim();
    }
    @Override public PaymentProvider getPaymentProvider() { return PaymentProvider.VNPAY; }
    @Override public String createPaymentUrl(String orderId, double amount, String description) {
        try {
            Map<String,String> p = new TreeMap<>();
            p.put("vnp_Version","2.1.0"); p.put("vnp_Command","pay"); p.put("vnp_TmnCode",tmn);
            p.put("vnp_Amount",String.valueOf(Math.round(amount*100))); p.put("vnp_CurrCode","VND");
            p.put("vnp_TxnRef",orderId); p.put("vnp_OrderInfo",description == null ? "Booking " + orderId : description);
            p.put("vnp_OrderType","other"); p.put("vnp_Locale","vn"); p.put("vnp_ReturnUrl",returnUrl);
            p.put("vnp_IpAddr","127.0.0.1"); p.put("vnp_CreateDate",LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String query = encode(p);
            String hash = hmac(query);
            return endpoint + "?" + query + "&vnp_SecureHash=" + hash;
        } catch (Exception e) { throw new IllegalStateException("Could not create VNPay payment",e); }
    }
    @Override public WebhookResult handleWebhook(String payload, String signature) {
        try {
            Map<String,String> p = new TreeMap<>();
            for (String pair : payload.split("&")) { int i=pair.indexOf('='); if(i>0) p.put(java.net.URLDecoder.decode(pair.substring(0,i),StandardCharsets.UTF_8),java.net.URLDecoder.decode(pair.substring(i+1),StandardCharsets.UTF_8)); }
            String supplied = p.remove("vnp_SecureHash"); p.remove("vnp_SecureHashType");
            String computed=hmac(encode(p)); boolean valid=supplied!=null && MessageDigest.isEqual(computed.getBytes(StandardCharsets.UTF_8),supplied.toLowerCase().getBytes(StandardCharsets.UTF_8));
            long amount=Long.parseLong(p.getOrDefault("vnp_Amount","0"))/100;
            return new WebhookResult(valid,valid && "00".equals(p.get("vnp_ResponseCode")) && "00".equals(p.get("vnp_TransactionStatus")),p.get("vnp_TxnRef"),p.get("vnp_TransactionNo"),amount,payload);
        } catch(Exception e) { return new WebhookResult(false,false,null,null,0,payload); }
    }
    private String encode(Map<String,String> p) { return p.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&")); }
    private String hmac(String data) throws Exception { Mac m=Mac.getInstance("HmacSHA512");m.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA512"));return HexFormat.of().formatHex(m.doFinal(data.getBytes(StandardCharsets.UTF_8))); }
}
