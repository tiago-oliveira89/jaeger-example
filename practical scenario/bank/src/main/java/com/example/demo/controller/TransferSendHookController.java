package com.example.demo.controller;

import com.example.demo.TracerConfig;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/hook")
public class TransferSendHookController {

    private final Tracer tracer;

    @Autowired
    private RestTemplate restTemplate;

    private final String hookUrl = "http://localhost:8081/transfer/receive";

    private TransferSendHookController() {
        this.tracer = TracerConfig.initTracer("bank.transfer-send");
    }

    @PostMapping
    public ResponseEntity sendHook(@RequestBody TransferHook transferHook) throws JsonProcessingException {
        Span span = tracer.buildSpan("send-hook").start();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setTag("paymentId",transferHook.getPaymentId());
            span.setTag("payload", objectMapper.writeValueAsString(transferHook));
            span.setTag("url",hookUrl);
            Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CONSUMER);
            Tags.HTTP_METHOD.set(tracer.activeSpan(), HttpMethod.POST.name());
            try {
                restTemplate.postForObject(hookUrl, transferHook, String.class);
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
