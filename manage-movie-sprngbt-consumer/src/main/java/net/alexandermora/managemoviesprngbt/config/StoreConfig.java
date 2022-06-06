package net.alexandermora.managemoviesprngbt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.service.FailureService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableKafka
@Slf4j
public class StoreConfig
{

    private final ObjectMapper objectMapper;

    public static final  String RETRY = "RETRY";
    public static final  String DEAD = "DEAD";
    public static final String SUCCESS = "SUCCESS";

    @Autowired
    private FailureService failureService;

    ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, e) ->
    {
        log.error("Exception in consumerRecordRecoverer : {} ", e.getMessage(), e);
        var recordData = (ConsumerRecord<Integer, String>)consumerRecord;
        if (e.getCause() instanceof RecoverableDataAccessException)
        {
            //recovery logic
            log.info("Inside Recovery");
            failureService.saveFailedRecord(recordData, e, RETRY);
        }
        else
        {
            // non-recovery logic
            log.info("Inside Non-Recovery");
            failureService.saveFailedRecord(recordData, e, DEAD);
        }
    };



    public DefaultErrorHandler errorHandler(){

        var exceptionsToIgnoreList = List.of(IllegalArgumentException.class);

        var expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);
        expBackOff.setMultiplier(2.0);
        expBackOff.setMaxInterval(2_000L);

        var errorHandler = new DefaultErrorHandler(consumerRecordRecoverer, expBackOff);

        exceptionsToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);

        errorHandler.setRetryListeners(((recordData, ex, deliveryAttempt) ->
                log.info("Failed Record in Retry Listener, Exception : {} , deliveryAttempt : {} "
                ,ex.getMessage(), deliveryAttempt)));

        return  errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }
}
