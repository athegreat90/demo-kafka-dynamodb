package net.alexandermora.managemoviesprngbt.consumer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.alexandermora.managemoviesprngbt.domain.FailureRecord;
import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.domain.UserOrder;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserLikeMovieMapper;
import net.alexandermora.managemoviesprngbt.mapper.UserRentMapper;
import net.alexandermora.managemoviesprngbt.repo.FailureRecordRepo;
import net.alexandermora.managemoviesprngbt.repo.UserBuyRepo;
import net.alexandermora.managemoviesprngbt.repo.UserRentRepo;
import net.alexandermora.managemoviesprngbt.service.MovieBuyService;
import net.alexandermora.managemoviesprngbt.service.MovieRentService;
import net.alexandermora.managemoviesprngbt.service.UserLikeService;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.alexandermora.managemoviesprngbt.mapper.UserOrderMapper.INSTANCE;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
        , "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
        , "retryListener.startup=false"})
@EmbeddedKafka(partitions = 3, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}, topics = {Constants.MOVIE_BUY, Constants.MOVIE_RENT, Constants.USER_LIKE})
public class ConsumerTest
{
    private static final Logger log = LoggerFactory.getLogger(ConsumerTest.class);

    @SpyBean
    private MovieBuyEventsConsumer movieBuyEventsConsumer;

    @SpyBean
    private MovieBuyService movieBuyService;

    @SpyBean
    private MovieRentEventsConsumer movieRentEventsConsumer;

    @SpyBean
    private MovieRentService movieRentService;

    @SpyBean
    private UserLikeEventsConsumer userLikeEventsConsumer;

    @SpyBean
    private UserLikeService userLikeService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    private JsonHelper jsonHelper;

    private static DynamoDBProxyServer server;

    @SpyBean
    private UserBuyRepo userBuyRepo;

    @SpyBean
    private UserRentRepo userRentRepo;

    @Autowired
    private FailureRecordRepo failureRecordRepo;

    private Consumer<Integer, String> consumer;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @BeforeAll
    public static void setupClass() throws Exception {
        System.setProperty("sqlite4java.library.path", "native-libs");
        String port = "8000";
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", port});
        server.start();
    }

    @AfterAll
    public static void teardownClass() throws Exception {
        server.stop();
    }

    @BeforeEach
    void setUp() {

        var container = endpointRegistry.getListenerContainers()
                .stream().filter(messageListenerContainer ->
                        Objects.equals(messageListenerContainer.getGroupId(), Constants.LISTENER_GROUP))
                .collect(Collectors.toList()).get(0);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        try
        {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(UserOrder.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);

            tableRequest = dynamoDBMapper.generateCreateTableRequest(FailureRecord.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);

            tableRequest = dynamoDBMapper.generateCreateTableRequest(UserMovieLike.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);

            tableRequest = dynamoDBMapper.generateCreateTableRequest(UserRent.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        }
        catch (Exception e)
        {

        }

    }

    @AfterEach
    void tearDown() {

    }


    @Test
    void onMessage_buy() throws InterruptedException, JsonProcessingException, ExecutionException
    {

        var element = new UserOrderDto();
        element.setCount(1.0D);
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        var orders = INSTANCE.getOrders(body).stream().map(o ->
        {
            o.setId(UUID.randomUUID().toString());
            return o;
        }).collect(Collectors.toList());
        when(userBuyRepo.saveAll(anyList())).thenReturn(orders);

        //given
        String json = jsonHelper.getJson(body);
        kafkaTemplate.setDefaultTopic(Constants.MOVIE_BUY);
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(movieBuyEventsConsumer, times(2)).onMessage(isA(ConsumerRecord.class));
        verify(movieBuyService, times(2)).processBuyMovie(isA(ConsumerRecord.class));
    }

    @Test
    void onMessage_buy_is_empty() throws InterruptedException, JsonProcessingException, ExecutionException {

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
    void onMessage_buy_is_null() throws InterruptedException, JsonProcessingException, ExecutionException {

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

    @Test
    void onMessage_rent() throws InterruptedException, JsonProcessingException, ExecutionException
    {

        var element = new UserRentDto();
        element.setDateBegin(DateTime.now());
        element.setDateEnd(DateTime.now().plusDays(2));
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        var orders = UserRentMapper.INSTANCE.getDomains(body).stream().map(o ->
        {
            o.setId(UUID.randomUUID().toString());
            return o;
        }).collect(Collectors.toList());
        when(userRentRepo.saveAll(anyList())).thenReturn(orders);

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
    void onMessage_rent_is_empty() throws InterruptedException, JsonProcessingException, ExecutionException {

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
    void onMessage_rent_is_null() throws InterruptedException, JsonProcessingException, ExecutionException {

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

    @Test
    void onMessage_user_like() throws InterruptedException, JsonProcessingException, ExecutionException
    {

        var element = new UserLikeDto();
        element.setListMovies(List.of("abc123", "aa123"));
        element.setUsername("Demo1");

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

    @Test
    void onMessage_user_like_without_username() throws InterruptedException, JsonProcessingException, ExecutionException
    {

        var element = new UserLikeDto();
        element.setListMovies(List.of("abc123", "aa123"));
        element.setUsername(null);

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

    @Test
    void onMessage_user_like_without_movie_list() throws InterruptedException, JsonProcessingException, ExecutionException
    {

        var element = new UserLikeDto();
        element.setListMovies(null);
        element.setUsername("a");

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
