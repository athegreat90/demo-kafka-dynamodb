package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.UserMovieLike;
import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserLikeMovieMapper
{
    UserLikeMovieMapper INSTANCE = Mappers.getMapper(UserLikeMovieMapper.class);
    @Mapping(target = "username", source = "username")
    @Mapping(target = "listMovies", source = "listMovies")
    UserMovieLike getDomains(UserLikeDto body);
}
