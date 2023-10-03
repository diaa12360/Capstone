package com.atypon.client.configuration;

import com.atypon.client.service.RestTemplateErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ExtractingResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

    private final RestTemplateBuilder builder;
    @Autowired
    public ApplicationConfiguration(RestTemplateBuilder builder){
        this.builder = builder;
    }
    @Bean
    public RestTemplate getRestTemplate(){
        return builder.errorHandler(new RestTemplateErrorHandler()).build();
    }
    @Bean
    public HttpHeaders headers(){
        return new HttpHeaders();
    }

    @Bean
    public DefaultResponseErrorHandler defaultResponseErrorHandler(){
        return new ExtractingResponseErrorHandler();
    }
}
