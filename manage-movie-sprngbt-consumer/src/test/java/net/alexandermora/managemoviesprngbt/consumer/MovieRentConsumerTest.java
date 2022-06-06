package net.alexandermora.managemoviesprngbt.consumer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.alexandermora.managemoviesprngbt.domain.FailureRecord;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.repo.UserRentRepo;
import net.alexandermora.managemoviesprngbt.service.MovieRentService;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class MovieRentConsumerTest {

    private static final Logger log = LoggerFactory.getLogger(MovieRentConsumerTest.class);

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
    private MovieRentEventsConsumer movieRentEventsConsumer;

    @SpyBean
    private MovieRentService movieRentService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private UserRentRepo userRentRepo;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @BeforeEach
    void setUp()
    {
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        try
        {
            var tableRequest = dynamoDBMapper.generateCreateTableRequest(FailureRecord.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);

            tableRequest = dynamoDBMapper.generateCreateTableRequest(UserRent.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        } catch (Exception e) {

        }

    }

    @Test
    void givenUserRent_whenAssertingSave_thenExecuteOk() throws InterruptedException, JsonProcessingException, ExecutionException {

        var element = new UserRentDto();
        element.setDateBegin(DateTime.now());
        element.setDateEnd(DateTime.now().plusDays(2));
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        //given
        String json = jsonHelper.getJson(body);
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_RENT);
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieRentEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(movieRentService, times(1)).processRentMovie(isA(ConsumerRecord.class));

        var countTest = userRentRepo.count();

        assertEquals(1, countTest);
    }

    @ParameterizedTest
    @CsvSource({"abc123,", "abc123,demo1", ",demo1"})
    void givenUserRentWithoutFields_whenAssertingSave_thenExecuteOk(String movie, String username) throws InterruptedException, JsonProcessingException, ExecutionException {

        var element = new UserRentDto();
        element.setDateBegin(DateTime.now());
        element.setDateEnd(DateTime.now().plusDays(2));
        element.setMovie(movie);
        element.setUsername(username);

        var body = List.of(element);

        //given
        String json = jsonHelper.getJson(body);
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_RENT);
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieRentEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(movieRentService, times(1)).processRentMovie(isA(ConsumerRecord.class));
    }

    @Test
    void givenUserRentEmptyOrder_whenAssertingSave_thenReturnError() throws InterruptedException, JsonProcessingException, ExecutionException {

        //given
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_RENT);
        kafkaTemplate.sendDefault("[]").get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieRentEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(movieRentService, times(1)).processRentMovie(isA(ConsumerRecord.class));
    }

    @Test
    void givenUserRentNullOrder_whenAssertingSave_thenReturnError() throws InterruptedException, JsonProcessingException, ExecutionException {

        //given
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_RENT);
        kafkaTemplate.sendDefault(null).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieRentEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(movieRentService, times(1)).processRentMovie(isA(ConsumerRecord.class));
    }
}
