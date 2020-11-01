package com.example.demo;

import com.google.common.collect.ImmutableMap;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lesson2")
public class Tutorial2Controller {

    private final Tracer tracer;

    private Tutorial2Controller() {
       this.tracer = TracerConfig.initTracer("hello-world2");
    }

    @GetMapping("/{name}")
    public String sayHello(@PathVariable("name") String name) {
        Span span = tracer.buildSpan("say-hello").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setTag("hello-to", name);
            //os spans que são abertos dentro desse span automaticamente se tornam filhos dele
            String helloStr = formatString(name);
            printHello(helloStr);
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private String formatString(String helloTo) {
        Span span = tracer.buildSpan("formatString").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private void printHello(String helloStr) {
        Span span = tracer.buildSpan("printHello").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            System.out.println(helloStr);
            span.log(ImmutableMap.of("event", "println"));
        } finally {
            span.finish();
        }
    }
}
