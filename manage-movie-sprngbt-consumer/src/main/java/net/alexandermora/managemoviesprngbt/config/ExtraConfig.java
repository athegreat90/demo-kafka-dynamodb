package net.alexandermora.managemoviesprngbt.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.alexandermora.managemoviesprngbt.domain.FailureRecord;
import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.domain.UserOrder;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Log4j2
@Component
@RequiredArgsConstructor
public class ExtraConfig
{
    private final AmazonDynamoDB amazonDynamoDB;

    @Bean
    public ObjectMapper objectMapper()
    {
        return new ObjectMapper()
                .registerModule(new JodaModule());
    }

    @PostConstruct
    private void init()
    {
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

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

            tableRequest = dynamoDBMapper.generateCreateTableRequest(FailureRecord.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        }
        catch (Exception e)
        {
            log.error("Creating table in post construct:", e);
        }
    }
}
