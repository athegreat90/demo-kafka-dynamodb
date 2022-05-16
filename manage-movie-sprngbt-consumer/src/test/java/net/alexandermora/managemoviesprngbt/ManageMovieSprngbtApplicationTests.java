package net.alexandermora.managemoviesprngbt;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.alexandermora.managemoviesprngbt.domain.Movie;
import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.domain.UserOrder;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import net.alexandermora.managemoviesprngbt.repo.UserBuyRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "amazon.dynamodb.endpoint=http://192.168.68.74:8000/",
        "amazon.aws.accessKey=test1",
        "amazon.aws.secretKey=test231" })
class ManageMovieSprngbtApplicationTests
{
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    UserBuyRepo repository;

    @BeforeEach
    void setup() throws Exception
    {
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        try
        {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(UserOrder.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
            tableRequest = dynamoDBMapper.generateCreateTableRequest(UserMovieLike.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
            tableRequest = dynamoDBMapper.generateCreateTableRequest(UserRent.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
//        repository.deleteAll();
//
//        dynamoDBMapper.batchDelete(repository.findAll());
    }

    @Test
    @Order(1)
    void testSaveData()
    {
        repository.deleteAll();
        var buy = new UserOrder();

        buy.setUsername("username1");
        buy.setMovie("abc-123");
        buy.setCount(2.0D);

        buy = repository.save(buy);
//
        System.out.println("Json = " + getJson(buy));
    }

    @Test
    @Order(1)
    void getAll()
    {
        var movies = repository.findAll();
        System.out.println("Json = " + getJson(movies));
    }

    String getJson(Object o)
    {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
