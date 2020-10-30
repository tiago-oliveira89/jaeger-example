package com.example.demo.controller;

import com.example.demo.model.Endereco;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/endereco")
public class EnderecoController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url}")
    private String url;

    @GetMapping("/{cep}")
    public Endereco getEndereco(@PathVariable("cep") String cep) {
        Map<String,String> urlParam = new HashMap<>();
        String url = getUrl(cep, urlParam);
        return restTemplate.getForObject(url, Endereco.class);
    }

    private String getUrl(String cep, Map<String, String> urlParam) {
        urlParam.put("cep", cep);
        return UriComponentsBuilder
            .fromUriString(url)
            .buildAndExpand(urlParam)
            .toUri().toString();
    }
}
