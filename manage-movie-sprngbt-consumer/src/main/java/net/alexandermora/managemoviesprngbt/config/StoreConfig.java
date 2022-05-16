package net.alexandermora.managemoviesprngbt.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.error.KafkaCustomException;
import net.alexandermora.managemoviesprngbt.service.MovieBuyService;
import net.alexandermora.managemoviesprngbt.service.MovieRentService;
import net.alexandermora.managemoviesprngbt.service.UserLikeService;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@EnableKafka
@Slf4j
@Configuration
public class StoreConfig
{

    private final MovieBuyService movieBuyService;

    private final MovieRentService movieRentService;

    private final UserLikeService userLikeService;

    private final KafkaProperties kafkaProperties;

    private final ObjectMapper objectMapper;

    private String getJson(Object o)
    {
        try
        {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer, ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory)
    {
        log.info("Props: {}" , getJson(kafkaProperties.getProperties()));
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory
                .getIfAvailable(() -> new DefaultKafkaConsumerFactory<>(this.kafkaProperties.buildConsumerProperties())));
        factory.setConcurrency(3);
        factory.setErrorHandler(((thrownException, data) ->
                log.info("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), data)));

        factory.setStatefulRetry(Boolean.TRUE);
        factory.setRecoveryCallback((context ->
        {
            if(context.getLastThrowable().getCause() instanceof RecoverableDataAccessException)
            {
                //invoke recovery logic
                log.info("Inside the recoverable logic");
                Arrays.asList(context.attributeNames()).forEach(attributeName ->
                {
                    log.info("Attribute name is : {} ", attributeName);
                    log.info("Attribute Value is : {} ", context.getAttribute(attributeName));
                });

                ConsumerRecord<Integer, String> consumerRecord = (ConsumerRecord<Integer, String>) context.getAttribute("record");
                switch (consumerRecord.topic())
                {
                    case Constants.MOVIE_BUY:
                    {
                        movieBuyService.handleRecovery(consumerRecord);
                        break;
                    }
                    case Constants.MOVIE_RENT:
                    {
                        movieRentService.handleRecovery(consumerRecord);
                        break;
                    }
                    case Constants.USER_LIKE:
                    {
                        userLikeService.handleRecovery(consumerRecord);
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
            }
            else
            {
                log.info("Inside the non recoverable logic");
                throw new KafkaCustomException(context.getLastThrowable().getMessage());
            }

            return null;
        }));
        return factory;
    }
}
