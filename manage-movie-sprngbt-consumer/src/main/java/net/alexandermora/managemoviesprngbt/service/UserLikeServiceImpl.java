package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.helper.KafkaHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserLikeMovieMapper;
import net.alexandermora.managemoviesprngbt.repo.UserLikeRepo;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserLikeServiceImpl implements UserLikeService
{
    private final ObjectMapper objectMapper;

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final UserLikeRepo userLikeRepo;

    private final UserLikeMovieMapper userLikeMovieMapper;

    private final JsonHelper jsonHelper;

    private final KafkaHelper kafkaHelper;

    @Override
    public void processUserLike(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException
    {
        var userLikes = objectMapper.readValue(consumerRecord.value(), UserLikeDto.class);

        if (Objects.isNull(userLikes))
        {
            return;
        }

        var domains = userLikeMovieMapper.getDomains(userLikes);

        var response = userLikeRepo.save(domains);

        log.info("User buy userLikes: {}", jsonHelper.getJson(response));
    }

    @Override
    public void handleRecovery(ConsumerRecord<Integer,String> consumerRecord)
    {

        Integer key = consumerRecord.key();
        String message = consumerRecord.value();

        ListenableFuture<SendResult<Integer,String>> listenableFuture = kafkaTemplate.send(Constants.USER_LIKE, key, message);
        listenableFuture.addCallback(new ListenableFutureCallback<>()
        {
            @Override
            public void onFailure(Throwable ex) {
                kafkaHelper.handleFailure(key, message, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                kafkaHelper.handleSuccess(key, message, result);
            }
        });
    }

}
