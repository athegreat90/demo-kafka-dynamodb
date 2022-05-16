package net.alexandermora.managemoviesprngbt.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaHelper
{
    public void handleFailure(Integer key, String value, Throwable ex)
    {
        log.error("Error Sending the Message: {} with the key: {}, and the exception is {}", value, key, ex.getMessage());
    }

    public void handleSuccess(Integer key, String value, SendResult<Integer, String> result)
    {
        log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }
}
