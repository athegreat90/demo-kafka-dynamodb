package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.helper.KafkaHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserOrderMapper;
import net.alexandermora.managemoviesprngbt.repo.UserBuyRepo;
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
public class MovieBuyServiceImpl implements MovieBuyService
{
    private final ObjectMapper objectMapper;

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final UserBuyRepo userBuyRepo;

    private final UserOrderMapper userOrderMapper;

    private final JsonHelper jsonHelper;

    private final KafkaHelper kafkaHelper;

    @Override
    public void processBuyMovie(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException
    {

        var request = consumerRecord.value();

        var orders = objectMapper.readValue(request, new TypeReference<List<UserOrderDto>>(){});

        if (Objects.isNull(orders) || orders.isEmpty())
        {
            return;
        }

        var domains = userOrderMapper.getOrders(orders);

        var response = userBuyRepo.saveAll(domains);

        log.info("User buy orders: {}", jsonHelper.getJson(response));
    }


    @Override
    public void handleRecovery(ConsumerRecord<Integer,String> consumerRecord)
    {

        Integer key = consumerRecord.key();
        String message = consumerRecord.value();

        ListenableFuture<SendResult<Integer,String>> listenableFuture = kafkaTemplate.send(Constants.MOVIE_BUY, key, message);
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
