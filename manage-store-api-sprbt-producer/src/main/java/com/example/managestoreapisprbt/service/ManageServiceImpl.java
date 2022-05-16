package com.example.managestoreapisprbt.service;

import com.example.managestoreapisprbt.dto.UserLikeDto;
import com.example.managestoreapisprbt.dto.UserOrderDto;
import com.example.managestoreapisprbt.dto.UserRentDto;
import com.example.managestoreapisprbt.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ManageServiceImpl implements ManageService
{
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendBuyEvent(List<UserOrderDto> orderDto)
    {
        var value = transformData(orderDto);
        var key = UUID.randomUUID().toString();
        ListenableFuture<SendResult<String,String>> listenableFuture =  kafkaTemplate.send(Constants.MOVIE_BUY, key, value);
        manageListenableFuture(key, value, listenableFuture);
    }

    @Override
    public void sendLikeEvent(List<UserLikeDto> likeDto)
    {
        var value = transformData(likeDto);
        var key = UUID.randomUUID().toString();
        ListenableFuture<SendResult<String, String>> listenableFuture =  kafkaTemplate.send(Constants.USER_LIKE, key, value);
        manageListenableFuture(key, value, listenableFuture);
    }

    @Override
    public void sendRentEvent(List<UserRentDto> rentDto)
    {
        var value = transformData(rentDto);
        var key = UUID.randomUUID().toString();
        ListenableFuture<SendResult<String,String>> listenableFuture =  kafkaTemplate.send(Constants.MOVIE_RENT, key, value);
        manageListenableFuture(key, value, listenableFuture);
    }

    private String transformData(Object o)
    {
        try
        {
            return objectMapper.writeValueAsString(o);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private void manageListenableFuture(String key, String value, ListenableFuture<SendResult<String, String>> listenableFuture)
    {
        var listener = new ListenableFutureCallback<SendResult<String, String>>()
        {
            @Override
            public void onFailure(Throwable ex)
            {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<String, String> result)
            {
                handleSuccess(key, value, result);
            }
        };
        listenableFuture.addCallback(listener);
    }

    private void handleFailure(String key, String value, Throwable ex)
    {
        log.error("Error Sending the Message with the key: {} and value: {} and the exception is {}", key, value, ex.getMessage());
    }

    private void handleSuccess(String key, String value, SendResult<String, String> result)
    {
        log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }
}
