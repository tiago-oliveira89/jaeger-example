package com.example.demo;

import com.google.common.collect.ImmutableMap;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/lesson4")
public class Tutorial4Controller {

    @Autowired
    RestTemplate restTemplate;

    private final Tracer tracer;

    private Tutorial4Controller() {
        this.tracer = TracerConfig.initTracer("hello-world3");
    }

    @GetMapping("/{name}/{greeting}")
    public String saySomething(@PathVariable("name") String name,@PathVariable("greeting") String greeting) {
        Span span = tracer.buildSpan("say-hello").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setBaggageItem("greeting",greeting);
            return sayHello(name);
        } finally {
            span.finish();
        }
    }

    private String sayHello(String helloTo) {
        Span span = tracer.buildSpan("say-hello").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setTag("hello-to", helloTo);
            String greeting = span.getBaggageItem("greeting");
            if (greeting == null) {
                greeting = "Hello";
            }
            String helloStr = String.format("%s, %s!", greeting, helloTo);
            return helloStr;
        }
        catch (Exception ex) {
            Tags.ERROR.set(tracer.activeSpan(), true);
            tracer.activeSpan().log(ImmutableMap.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, ex));
            throw new RuntimeException(ex);
        } finally {
            span.finish();
        }
    }
}
