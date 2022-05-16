package net.alexandermora.managemoviesprngbt.repo;

import net.alexandermora.managemoviesprngbt.domain.UserOrder;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface UserRentRepo extends DynamoDBCrudRepository<UserRent, String>
{

}
