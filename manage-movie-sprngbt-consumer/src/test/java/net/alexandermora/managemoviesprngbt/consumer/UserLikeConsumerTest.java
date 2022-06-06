package net.alexandermora.managemoviesprngbt.consumer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.alexandermora.managemoviesprngbt.domain.FailureRecord;
import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserLikeMovieMapper;
import net.alexandermora.managemoviesprngbt.repo.UserLikeRepo;
import net.alexandermora.managemoviesprngbt.service.UserLikeService;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.util.StringUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.dynamodb.DynaliteContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class UserLikeConsumerTest
{
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
    private UserLikeEventsConsumer userLikeEventsConsumer;

    @SpyBean
    private UserLikeService userLikeService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private UserLikeRepo userLikeRepo;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @BeforeEach
    void setUp()
    {
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        try
        {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(FailureRecord.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);

            tableRequest = dynamoDBMapper.generateCreateTableRequest(UserMovieLike.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        }
        catch (Exception e)
        {
            //
        }
    }

    @Test
    void givenUserLike_whenAssertingSave_thenExecuteOk() throws InterruptedException, JsonProcessingException, ExecutionException
    {
        if (userLikeRepo.count() > 0)
        {
            userLikeRepo.deleteAll();
        }

        var element = new UserLikeDto();
        element.setListMovies(List.of("abc123", "aa123"));
        element.setUsername("Demo1");

        //given
        String json = jsonHelper.getJson(element);
        kafkaTemplate.setDefaultTopic(Constants.USER_LIKE);
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(userLikeEventsConsumer, atLeast(1)).onMessage(isA(ConsumerRecord.class));
        verify(userLikeService, atLeast(1)).processUserLike(isA(ConsumerRecord.class));

        var countTest = userLikeRepo.count();
        assertEquals(1, countTest);
    }

    @ParameterizedTest
    @CsvSource({"abc123,", "abc123,demo1;demo2", ",demo1;demo2", "abc123,;"})
    void givenUserLikeOrderWithoutFields_whenAssertingSave_thenReturnError(String username, String movies) throws InterruptedException, JsonProcessingException, ExecutionException {

        var movieList = StringUtils.hasText(movies) ? Arrays.asList(movies.split(";")) : null;
        var element = new UserLikeDto();
        element.setUsername(username);
        element.setListMovies(movieList);

        var userMovieLike = UserLikeMovieMapper.INSTANCE.getDomains(element);
        userMovieLike.setId(UUID.randomUUID().toString());


        //given
        String json = jsonHelper.getJson(element);
        kafkaTemplate.setDefaultTopic(Constants.USER_LIKE);
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(userLikeEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(userLikeService, times(1)).processUserLike(isA(ConsumerRecord.class));
    }
}
