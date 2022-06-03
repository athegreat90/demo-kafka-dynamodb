package net.alexandermora.managemoviesprngbt.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.service.UserLikeService;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserLikeEventsConsumer
{
    private final UserLikeService userLikeService;

    @KafkaListener(topics = {Constants.USER_LIKE}, groupId = Constants.LISTENER_GROUP)
    public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException
    {
        log.info("ConsumerRecord : {} ", consumerRecord );
        userLikeService.processUserLike(consumerRecord);
    }
}
