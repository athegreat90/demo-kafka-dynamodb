package net.alexandermora.managemoviesprngbt.repo;

import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface UserLikeRepo extends DynamoDBCrudRepository<UserMovieLike, String>
{

}
