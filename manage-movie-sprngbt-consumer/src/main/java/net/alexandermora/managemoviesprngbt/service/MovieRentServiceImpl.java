package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import net.alexandermora.managemoviesprngbt.error.KafkaCustomException;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserRentMapper;
import net.alexandermora.managemoviesprngbt.repo.UserRentRepo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class MovieRentServiceImpl implements MovieRentService
{
    private final ObjectMapper objectMapper;

    private final UserRentRepo userRentRepo;

    private final UserRentMapper userRentMapper;

    private final JsonHelper jsonHelper;

    @Override
    public void processRentMovie(ConsumerRecord<Integer, String> consumerRecord)
    {
        try
        {
            var request = consumerRecord.value();

            if (!StringUtils.hasText(request))
            {
                return;
            }

            var rents = objectMapper.readValue(request, new TypeReference<List<UserRentDto>>(){});

            if (Objects.isNull(rents) || rents.isEmpty())
            {
                return;
            }

            var domains = userRentMapper.getDomains(rents);

            log.info("Request Kafka: {} - Obj: {}", request, domains);

            domains = domains.stream().filter(this::validateDomain).collect(Collectors.toList());

            if (domains.isEmpty())
            {
                log.warn("Invalid Orders");
                throw new KafkaCustomException("Invalid orders");
            }

            var response = userRentRepo.saveAll(domains);

            log.info("User buy rents: {}", jsonHelper.getJson(response));
        }
        catch (Exception e)
        {
            log.error("Rent", e);
        }
    }

    private boolean validateDomain(UserRent userRent)
    {
        return Objects.nonNull(userRent.getDateBegin()) && Objects.nonNull(userRent.getDateEnd()) && StringUtils.hasText(userRent.getMovie()) && StringUtils.hasText(userRent.getUsername());
    }

}
