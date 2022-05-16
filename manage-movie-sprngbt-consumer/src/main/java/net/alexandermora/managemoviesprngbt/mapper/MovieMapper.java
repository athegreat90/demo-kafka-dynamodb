package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.Movie;
import net.alexandermora.managemoviesprngbt.dto.MovieDto;
import org.mapstruct.Mapper;

@Mapper()
public interface MovieMapper
{
    Movie getDomain(MovieDto dto);
}
