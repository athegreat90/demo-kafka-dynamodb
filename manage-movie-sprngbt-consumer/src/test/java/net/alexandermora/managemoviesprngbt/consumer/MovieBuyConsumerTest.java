package net.alexandermora.managemoviesprngbt.consumer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.repo.UserBuyRepo;
import net.alexandermora.managemoviesprngbt.service.MovieBuyService;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.dynamodb.DynaliteContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class MovieBuyConsumerTest
{
    private static Network network = Network.newNetwork();

    @Container
    private static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("latest"))
            .withNetwork(network);

    @Container
    public static DynaliteContainer dynamoDB = new DynaliteContainer(DockerImageName.parse("quay.io/testcontainers/dynalite").withTag("v1.2.1-1"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        var url = String.format("http://%s:%s", dynamoDB.getHost(), dynamoDB.getMappedPort(4567));
        registry.add("amazon.dynamodb.endpoint", () -> url);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @SpyBean
    private MovieBuyEventsConsumer movieBuyEventsConsumer;

    @SpyBean
    private MovieBuyService movieBuyService;

    @Autowired
    private UserBuyRepo userBuyRepo;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;


    @Test
    void givenUserOrder_whenAssertingSave_thenExecuteOk() throws InterruptedException, JsonProcessingException, ExecutionException {

        var element = new UserOrderDto();
        element.setCount(1.0D);
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        //given
        String json = jsonHelper.getJson(body);
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_BUY);
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieBuyEventsConsumer, atLeast(1)).onMessage(isA(ConsumerRecord.class));
        verify(movieBuyService, atLeast(1)).processBuyMovie(isA(ConsumerRecord.class));

        var countTest = userBuyRepo.count();

        assertEquals(1, countTest);
    }

    @ParameterizedTest
    @CsvSource({"1,abc123,", ",abc123,demo1", "1,,demo1"})
    void givenUserOrderWithoutFields_whenAssertingSave_thenExecuteOk(Double count, String movie, String username) throws InterruptedException, JsonProcessingException, ExecutionException {

        var element = new UserOrderDto();
        element.setCount(count);
        element.setMovie(movie);
        element.setUsername(username);

        var body = List.of(element);

        //given
        String json = jsonHelper.getJson(body);
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_BUY);
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieBuyEventsConsumer, atLeast(1)).onMessage(isA(ConsumerRecord.class));
        verify(movieBuyService, atLeast(1)).processBuyMovie(isA(ConsumerRecord.class));
    }

    @Test
    void givenUserBuyEmptyOrder_whenAssertingSave_thenReturnError() throws InterruptedException, JsonProcessingException, ExecutionException {

        //given
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_BUY);
        kafkaTemplate.sendDefault("[]").get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieBuyEventsConsumer, times(2)).onMessage(isA(ConsumerRecord.class));
        verify(movieBuyService, times(2)).processBuyMovie(isA(ConsumerRecord.class));
    }

    @Test
    void givenUserBuyNullOrder_whenAssertingSave_thenReturnError() throws InterruptedException, JsonProcessingException, ExecutionException {

        //given
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_BUY);
        kafkaTemplate.sendDefault(null).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieBuyEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(movieBuyService, times(1)).processBuyMovie(isA(ConsumerRecord.class));
    }
}
