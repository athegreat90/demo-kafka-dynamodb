package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface MovieRentService
{
    void processRentMovie(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException;
    void handleRecovery(ConsumerRecord<Integer,String> consumerRecord);
}
