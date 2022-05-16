package net.alexandermora.managemoviesprngbt.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;
import org.joda.time.DateTime;

@Data
public class UserRentDto
{
    private String id;

    private DateTime dateBegin;

    private DateTime dateEnd;

    private String username;

    private String movie;
}
