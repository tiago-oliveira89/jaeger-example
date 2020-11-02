package com.example.demo.controller;

import com.example.demo.TracerConfig;
import com.example.demo.model.Transfer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfer")
public class TransferReceiveController {

    private final Tracer tracer;

    private TransferReceiveController() {
        this.tracer = TracerConfig.initTracer("bank.transfer-receive");
    }

    @PostMapping
    public ResponseEntity receiveTransfer(@RequestBody Transfer transfer) throws JsonProcessingException {
        Span span = tracer.buildSpan("receiver-transfer").start();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setTag("paymentId",transfer.getPaymentId());
            span.setTag("payload", objectMapper.writeValueAsString(transfer));
            HttpStatus httpStatus = validate(transfer);
            Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_PRODUCER);
            Tags.HTTP_STATUS.set(tracer.activeSpan(), httpStatus.value());
            Tags.HTTP_METHOD.set(tracer.activeSpan(), HttpMethod.POST.name());
            return new ResponseEntity(httpStatus);
        } finally {
            span.finish();
        }
    }

    private HttpStatus validate(Transfer transfer) {
        Span span = tracer.buildSpan("validate-transfer").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            if(transfer.getAmount() == null) {
                span.setTag("validation_error","amount_not_informed");
                return HttpStatus.BAD_REQUEST;
            }
            if(transfer.getPaymentId() == null) {
                span.setTag("validation_error","payment_id_not_informed");
                return HttpStatus.BAD_REQUEST;
            }
            if(transfer.getPaymentAccount() == null) {
                span.setTag("validation_error","payment_account_not_informed");
                return HttpStatus.BAD_REQUEST;
            }
            if(transfer.getReceiverAccount() == null) {
                span.setTag("validation_error","receiver_account_not_informed");
                return HttpStatus.BAD_REQUEST;
            }
            return HttpStatus.CREATED;
            } finally {
                span.finish();
            }
    }


}
