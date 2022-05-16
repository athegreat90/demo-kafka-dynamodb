package com.example.managestoreapisprbt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreConfig
{
    @Bean
    public ObjectMapper objectMapper()
    {
        return new ObjectMapper().registerModule(new JodaModule());
    }


}
