package com.bambooairways.booking.service;

import com.bambooairways.booking.dto.WebhookResult;
import com.bambooairways.enums.PaymentProvider;
import com.bambooairways.interfaces.PaymentStategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Component
public class StrideStrategy implements PaymentStategy {
    @Value("${STRIPE_SECRET_KEY:}") private String secret;
    @Value("${STRIPE_WEBHOOK_SECRET:}") private String webhookSecret;
    @Value("${FRONTEND_URL:http://localhost:3000}") private String frontend;

    @PostConstruct
    void normalizeConfig() {
        secret = secret.trim();
        webhookSecret = webhookSecret.trim();
        frontend = frontend.trim();
    }
    private final ObjectMapper json=new ObjectMapper();
    @Override public PaymentProvider getPaymentProvider(){return PaymentProvider.STRIPE;}
    @Override public String createPaymentUrl(String orderId,double amount,String description){
        try {
            Map<String,String> form=Map.of("mode","payment","success_url",frontend+"/checkout/result?session_id={CHECKOUT_SESSION_ID}","cancel_url",frontend+"/checkout/result?cancelled=true","client_reference_id",orderId,"metadata[orderId]",orderId,"line_items[0][price_data][currency]","vnd","line_items[0][price_data][unit_amount]",String.valueOf(Math.round(amount)),"line_items[0][price_data][product_data][name]",description==null?"Bamboo Airways booking":description,"line_items[0][quantity]","1");
            String body=form.entrySet().stream().map(e->java.net.URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+java.net.URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
            JsonNode response=RestClient.create().post().uri("https://api.stripe.com/v1/checkout/sessions").contentType(MediaType.APPLICATION_FORM_URLENCODED).headers(h->h.setBasicAuth(secret,"")).body(body).retrieve().body(JsonNode.class);
            if(response==null || !response.hasNonNull("url"))throw new IllegalStateException("Stripe response did not include checkout URL");
            return response.get("url").asText();
        }catch(Exception e){throw new IllegalStateException("Could not create Stripe Checkout session",e);}
    }
    @Override public WebhookResult handleWebhook(String payload,String signature){
        try{
            String timestamp=null, v1=null;
            for(String part:signature==null?new String[0]:signature.split(",")){String[] kv=part.split("=",2);if(kv.length==2){if("t".equals(kv[0]))timestamp=kv[1];if("v1".equals(kv[0]))v1=kv[1];}}
            if(timestamp==null||v1==null)return new WebhookResult(false,false,null,null,0,payload);
            String expected=HexFormat.of().formatHex(hmac(timestamp+"."+payload));
            boolean valid=MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),v1.getBytes(StandardCharsets.UTF_8));
            JsonNode event=json.readTree(payload), object=event.path("data").path("object");
            String type=event.path("type").asText();
            String order=object.path("metadata").path("orderId").asText(object.path("client_reference_id").asText());
            boolean success=valid && "checkout.session.completed".equals(type) && "paid".equals(object.path("payment_status").asText());
            return new WebhookResult(valid,success,order,object.path("payment_intent").asText(object.path("id").asText()),object.path("amount_total").asLong(),payload);
        }catch(Exception e){return new WebhookResult(false,false,null,null,0,payload);}
    }
    private byte[] hmac(String value)throws Exception{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));}
}
