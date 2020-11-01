package com.example.demo;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.collect.ImmutableMap;

@RestController
@RequestMapping("/lesson1")
public class Tutorial1Controller {

    private final Tracer tracer;

    private Tutorial1Controller() {
       this.tracer = TracerConfig.initTracer("hello-world");
    }


    @GetMapping("/{name}")
    public String sayHello(@PathVariable("name") String name) {
        Span span = tracer.buildSpan("say-high-five").start();
        String hello = String.format("Hello, %s!", name);
        span.log(ImmutableMap.of("event", "string-format", "value", name));
        span.setTag("high-five-to", name);
        span.finish();
        return hello;
    }


}
