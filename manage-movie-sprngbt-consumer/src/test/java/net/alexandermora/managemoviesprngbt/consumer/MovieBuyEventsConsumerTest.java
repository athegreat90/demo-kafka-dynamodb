package net.alexandermora.managemoviesprngbt.consumer;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserOrderMapper;
import net.alexandermora.managemoviesprngbt.repo.MovieRepo;
import net.alexandermora.managemoviesprngbt.repo.UserBuyRepo;
import net.alexandermora.managemoviesprngbt.repo.UserLikeRepo;
import net.alexandermora.managemoviesprngbt.repo.UserRentRepo;
import net.alexandermora.managemoviesprngbt.service.MovieBuyService;
import net.alexandermora.managemoviesprngbt.service.MovieBuyServiceImpl;
import net.alexandermora.managemoviesprngbt.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class MovieBuyEventsConsumerTest
{
    private static final Logger log = LoggerFactory.getLogger(MovieBuyEventsConsumerTest.class);

    @Autowired
    private MovieBuyEventsConsumer movieBuyEventsConsumer;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private JsonHelper jsonHelper;

    @SpyBean
    private UserBuyRepo userBuyRepo;

    @SpyBean
    private MovieRepo movieRepo;

    @SpyBean
    private UserLikeRepo userLikeRepo;

    @SpyBean
    private UserRentRepo userRentRepo;

    @Autowired
    private UserOrderMapper userOrderMapper;

    private static DynamoDBProxyServer server;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    BlockingQueue<ConsumerRecord<Integer, String>> records;

    KafkaMessageListenerContainer<Integer, String> container;

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
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<Integer, String> consumerFactory = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(Constants.MOVIE_BUY);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<Integer, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterEach
    void tearDown() {
        container.stop();
    }


    @Test
    void onMessage() throws JsonProcessingException
    {

        var element = new UserOrderDto();
        element.setCount(1.0D);
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);



        kafkaTemplate.send(Constants.MOVIE_BUY, jsonHelper.getJson(body));

//        var domains = userOrderMapper.getOrders(body);
//
//        domains.stream().map(d -> {
//            d.setId(UUID.randomUUID().toString());
//            return d;
//        });
//
//        when(userBuyRepo.saveAll(anyList())).thenReturn(domains);


        // verify(consumer, timeout(1000).times(1)).listen(BankModelArgumentCaptor.capture(), topicArgumentCaptor.capture());

        ConsumerRecord<Integer, String> singleRecord = new ConsumerRecord<>(Constants.MOVIE_BUY, 1, 1, 1, jsonHelper.getJson(body));


        verify(movieBuyEventsConsumer, timeout(1000L).times(1)).onMessage(singleRecord);

//        verify(movieBuyEventsConsumer, times(1)).onMessage();



    }
}