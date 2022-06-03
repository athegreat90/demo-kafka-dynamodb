package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import net.alexandermora.managemoviesprngbt.error.KafkaCustomException;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserOrderMapper;
import net.alexandermora.managemoviesprngbt.repo.UserBuyRepo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class MovieBuyServiceImpl implements MovieBuyService
{
    private final ObjectMapper objectMapper;

    private final UserBuyRepo userBuyRepo;

    private final UserOrderMapper userOrderMapper;

    private final JsonHelper jsonHelper;

    @Override
    public void processBuyMovie(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException
    {

        var request = consumerRecord.value();

        if (!StringUtils.hasText(request))
        {
            return;
        }

        var orders = objectMapper.readValue(request, new TypeReference<List<UserOrderDto>>(){});

        log.info("Request Kafka: {} - Obj: {}", request, orders);

        if (Objects.isNull(orders))
        {
            log.warn("Orders null");
            return;
        }

        orders = orders.stream().filter(this::validateOrder).collect(Collectors.toList());

        if (orders.isEmpty())
        {
            log.warn("Invalid Orders");
            throw new KafkaCustomException("Invalid orders");
        }

        var domains = userOrderMapper.getOrders(orders);

        var response = userBuyRepo.saveAll(domains);

        log.info("User buy orders: {}", jsonHelper.getJson(response));
    }

    private boolean validateOrder(UserOrderDto o)
    {
        return StringUtils.hasText(o.getMovie()) && StringUtils.hasText(o.getUsername()) && Objects.nonNull(o.getCount());
    }
}
