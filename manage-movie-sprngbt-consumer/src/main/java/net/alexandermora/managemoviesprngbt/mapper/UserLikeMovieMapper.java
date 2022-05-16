package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.domain.UserRent;
import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface UserLikeMovieMapper
{
    @Mapping(target = "username", source = "username")
    @Mapping(target = "listMovies", source = "listMovies")
    UserMovieLike getDomains(UserLikeDto body);
}
