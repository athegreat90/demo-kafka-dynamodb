package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserLikeMovieMapper
{
    @Mapping(target = "username", source = "username")
    @Mapping(target = "listMovies", source = "listMovies")
    UserMovieLike getDomains(UserLikeDto body);
}
