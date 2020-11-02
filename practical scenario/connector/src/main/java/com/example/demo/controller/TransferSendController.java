package com.example.demo.controller;

import com.example.demo.TracerConfig;
import com.example.demo.model.Transfer;
import com.example.demo.model.TransferHook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/transfer")
public class TransferSendController {

    private final Tracer tracer;

    @Autowired
    private RestTemplate restTemplate;

    private final String transferUrl = "http://localhost:8080/transfer";

    private TransferSendController() {
        this.tracer = TracerConfig.initTracer("connector.transfer-send");
    }

    @PostMapping("/send")
    public ResponseEntity sendHook(@RequestBody Transfer transfer) throws JsonProcessingException {
        Span span = tracer.buildSpan("send-transfer").start();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setTag("paymentId",transfer.getPaymentId());
            span.setTag("payload", objectMapper.writeValueAsString(transfer));
            span.setTag("url", transferUrl);
            Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_PRODUCER);
            Tags.HTTP_METHOD.set(tracer.activeSpan(), HttpMethod.POST.name());
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(transferUrl, transfer, String.class);
                Tags.HTTP_STATUS.set(tracer.activeSpan(), responseEntity.getStatusCodeValue());
            }
            catch (Exception ex) {
                Tags.ERROR.set(tracer.activeSpan(), true);
                tracer.activeSpan().log(ImmutableMap.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, ex));
                throw new RuntimeException(ex);
            }
        }
        finally {
            span.finish();
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }

}
