package net.alexandermora.managemoviesprngbt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.domain.FailureRecord;
import net.alexandermora.managemoviesprngbt.repo.FailureRecordRepo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class FailureServiceImpl implements FailureService
{
    private final FailureRecordRepo failureRecordRepo;

    @Override
    public void saveFailedRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e, String status) {

        var failureRecord = new FailureRecord(null, consumerRecord.topic(),
                consumerRecord.key(), consumerRecord.value(), consumerRecord.partition()
                ,consumerRecord.offset(), e.getCause().getMessage(), status);

        failureRecordRepo.save(failureRecord);

    }
}

