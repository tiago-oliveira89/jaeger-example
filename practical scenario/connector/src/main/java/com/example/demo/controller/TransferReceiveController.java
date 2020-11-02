package com.example.demo.controller;

import com.example.demo.TracerConfig;
import com.example.demo.model.Transfer;
import com.example.demo.model.TransferHook;
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
        this.tracer = TracerConfig.initTracer("connector.transfer-receive");
    }

    @PostMapping("/receive")
    public ResponseEntity receiveTransfer(@RequestBody TransferHook transfer) throws JsonProcessingException {
        Span span = tracer.buildSpan("receiver-transfer").start();
        ObjectMapper objectMapper = new ObjectMapper();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setTag("paymentId",transfer.getPaymentId());
            span.setTag("payload", objectMapper.writeValueAsString(transfer));
            HttpStatus httpStatus = validate(transfer);
            Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CONSUMER);
            Tags.HTTP_STATUS.set(tracer.activeSpan(), httpStatus.value());
            Tags.HTTP_METHOD.set(tracer.activeSpan(), HttpMethod.POST.name());
            return new ResponseEntity(httpStatus);
        } finally {
            span.finish();
        }
    }

    private HttpStatus validate(TransferHook transferHook) {
        Span span = tracer.buildSpan("validate-transferHook").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            if(transferHook.getPaymentId() == null) {
                span.setTag("validation_error","payment_id_not_informed");
                return HttpStatus.BAD_REQUEST;
            }
            if(transferHook.getOperationId() == null) {
                span.setTag("validation_error","operation_id_not_informed");
                return HttpStatus.BAD_REQUEST;
            }
            return HttpStatus.CREATED;
            } finally {
                span.finish();
            }
    }


}
