package net.alexandermora.managemoviesprngbt.repo;

import net.alexandermora.managemoviesprngbt.domain.Movie;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface MovieRepo extends DynamoDBCrudRepository<Movie, String> {
}
