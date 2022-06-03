package net.alexandermora.managemoviesprngbt.repo;

import net.alexandermora.managemoviesprngbt.domain.FailureRecord;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

@EnableScan
public interface FailureRecordRepo extends DynamoDBCrudRepository<FailureRecord, String> {
}
