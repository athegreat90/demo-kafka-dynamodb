package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.helper.KafkaHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserRentMapper;
import net.alexandermora.managemoviesprngbt.repo.UserRentRepo;
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
public class MovieRentServiceImpl implements MovieRentService
{
    private final ObjectMapper objectMapper;

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final UserRentRepo userRentRepo;

    private final UserRentMapper userRentMapper;

    private final JsonHelper jsonHelper;

    private final KafkaHelper kafkaHelper;

    @Override
    public void processRentMovie(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException
    {
        try
        {
            var rents = objectMapper.readValue(consumerRecord.value(), new TypeReference<List<UserRentDto>>(){});

            if (Objects.isNull(rents) || rents.isEmpty())
            {
                return;
            }

            var domains = userRentMapper.getDomains(rents);

            var response = userRentRepo.saveAll(domains);

            log.info("User buy rents: {}", jsonHelper.getJson(response));
        }
        catch (Exception e)
        {
            log.error("Rent", e);
        }
    }

    @Override
    public void handleRecovery(ConsumerRecord<Integer,String> consumerRecord)
    {

        Integer key = consumerRecord.key();
        String message = consumerRecord.value();

        ListenableFuture<SendResult<Integer,String>> listenableFuture = kafkaTemplate.send(Constants.MOVIE_RENT, key, message);
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
