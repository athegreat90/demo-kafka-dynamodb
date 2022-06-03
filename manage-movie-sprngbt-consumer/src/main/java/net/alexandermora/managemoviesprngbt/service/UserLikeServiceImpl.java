package net.alexandermora.managemoviesprngbt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import net.alexandermora.managemoviesprngbt.helper.JsonHelper;
import net.alexandermora.managemoviesprngbt.mapper.UserLikeMovieMapper;
import net.alexandermora.managemoviesprngbt.repo.UserLikeRepo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserLikeServiceImpl implements UserLikeService
{
    private final ObjectMapper objectMapper;

    private final UserLikeRepo userLikeRepo;

    private final UserLikeMovieMapper userLikeMovieMapper;

    private final JsonHelper jsonHelper;

    @Override
    public void processUserLike(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException
    {
        var userLikes = objectMapper.readValue(consumerRecord.value(), UserLikeDto.class);

        if (Objects.isNull(userLikes))
        {
            return;
        }

        var domains = userLikeMovieMapper.getDomains(userLikes);

        if (!StringUtils.hasText(domains.getUsername()) || Objects.isNull(domains.getListMovies()) || domains.getListMovies().isEmpty())
        {
            return;
        }

        var response = userLikeRepo.save(domains);

        log.info("User buy userLikes: {}", jsonHelper.getJson(response));
    }

}
