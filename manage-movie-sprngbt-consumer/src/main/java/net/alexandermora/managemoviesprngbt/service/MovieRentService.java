package net.alexandermora.managemoviesprngbt.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface MovieRentService
{
    void processRentMovie(ConsumerRecord<Integer, String> consumerRecord);
}
