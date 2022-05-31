package com.example.managestoreapisprbt.controller;

import com.example.managestoreapisprbt.dto.UserLikeDto;
import com.example.managestoreapisprbt.dto.UserOrderDto;
import com.example.managestoreapisprbt.dto.UserRentDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class EmbeddedKafkaIntegrationTest
{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void givenBuyOrder_whenSendingToProducer_thenProcessOk() throws Exception
    {
        var url = "/api/v1/manage/buy";
        var element = new UserOrderDto();
        element.setCount(1.0D);
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        var json = transformData(body);

        mockMvc.perform(MockMvcRequestBuilders.post(url).content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void givenRentOrder_whenSendingToProducer_thenProcessOk() throws Exception
    {
        var url = "/api/v1/manage/rent";
        var element = new UserRentDto();
        element.setDateBegin(DateTime.now());
        element.setDateEnd(DateTime.now().plusDays(2));
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        var json = transformData(body);

        mockMvc.perform(MockMvcRequestBuilders.post(url).content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void givenLikeOrder_whenSendingToProducer_thenProcessOk() throws Exception
    {
        var url = "/api/v1/manage/like";
        var element = new UserLikeDto();
        element.setListMovies(List.of("abc123", "abc124"));
        element.setUsername("Demo1");

        var body = List.of(element);

        var json = transformData(body);

        mockMvc.perform(MockMvcRequestBuilders.post(url).content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    private String transformData(Object o)
    {
        try
        {
            return objectMapper.writeValueAsString(o);
        }
        catch (JsonProcessingException e)
        {
            return null;
        }
    }
}
