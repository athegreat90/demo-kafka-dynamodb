package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface MovieBuyService
{
    void processBuyMovie(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException;
}
