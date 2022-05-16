package net.alexandermora.managemoviesprngbt.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.service.MovieBuyService;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieBuyEventsConsumer
{
    private final MovieBuyService movieBuyService;

    public MovieBuyEventsConsumer(MovieBuyService movieBuyService) {
        this.movieBuyService = movieBuyService;
    }

    @KafkaListener(topics = {Constants.MOVIE_BUY})
    public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException
    {
        log.info("ConsumerRecord : {} ", consumerRecord );
        movieBuyService.processBuyMovie(consumerRecord);
    }
}
