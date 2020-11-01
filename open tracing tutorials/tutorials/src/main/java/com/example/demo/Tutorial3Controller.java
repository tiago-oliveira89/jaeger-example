package com.example.demo;

import com.google.common.collect.ImmutableMap;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/lesson3")
public class Tutorial3Controller {

    @Autowired
    RestTemplate restTemplate;

    private final Tracer tracer;

    private Tutorial3Controller() {
        this.tracer = TracerConfig.initTracer("hello-world3");
    }

    @GetMapping("/{name}")
    public String saySomething(@PathVariable("name") String name) {
        Span span = tracer.buildSpan("say-hello").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            String helloStr = formatString(name);
            sayHello(name);
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private String getHttp(String path, String name) {
        Map<String,String> urlParam = new HashMap<>();
        String url = getUrl(name, urlParam,path);

        Span activeSpan = tracer.activeSpan();
        Tags.SPAN_KIND.set(activeSpan, Tags.SPAN_KIND_CLIENT);
        Tags.HTTP_METHOD.set(activeSpan, "GET");
        Tags.HTTP_URL.set(activeSpan, url.toString());
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        Tags.HTTP_STATUS.set(tracer.activeSpan(), responseEntity.getStatusCodeValue());
        return responseEntity.getBody();
    }

    private void sayHello(String helloTo) {
        Span span = tracer.buildSpan("say-hello").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            span.setTag("hello-to", helloTo);
            String helloStr = formatString(helloTo);
            printHello(helloStr);
        }
        catch (Exception ex) {
            Tags.ERROR.set(tracer.activeSpan(), true);
            tracer.activeSpan().log(ImmutableMap.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, ex));
            throw new RuntimeException(ex);
        } finally {
            span.finish();
        }
    }

    private String formatString(String helloTo) {
        Span span = tracer.buildSpan("formatString").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            String helloStr = getHttp("http://localhost:8081/{name}", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        }catch (Exception ex) {
            Tags.ERROR.set(tracer.activeSpan(), true);
            tracer.activeSpan().log(ImmutableMap.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, ex));
            throw new RuntimeException(ex);
        } finally {
            span.finish();
        }
    }

    private void printHello(String helloStr) {
        Span span = tracer.buildSpan("printHello").start();
        try (Scope scope = tracer.scopeManager().activate(span,true)) {
            getHttp("http://localhost:8082/{name}",helloStr);
            span.log(ImmutableMap.of("event", "println"));
        } finally{
            span.finish();
        }
    }

    private String getUrl(String name, Map<String, String> urlParam,String url) {
        urlParam.put("name", name);
        return UriComponentsBuilder
                .fromUriString(url)
                .buildAndExpand(urlParam)
                .toUri().toString();
    }

}
