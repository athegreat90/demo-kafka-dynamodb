package net.alexandermora.managemoviesprngbt.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDynamoDBRepositories(basePackages = "net.alexandermora.managemoviesprngbt.repo")
public class DynamoDBConfig
{
    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.aws.accessKey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretKey}")
    private String amazonAWSSecretKey;

    @Value("${amazon.aws.region:us-east-1a}")
    private String amazonAwsRegion;

    @Bean
    public AmazonDynamoDB amazonDynamoDB()
    {
        var endpoint = new AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, amazonAwsRegion);
        var credentialsProvider = new AWSStaticCredentialsProvider(amazonAWSCredentials());
        return AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(credentialsProvider).withEndpointConfiguration(endpoint).build();
    }

    @Bean
    public AWSCredentials amazonAWSCredentials()
    {
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    }
}
