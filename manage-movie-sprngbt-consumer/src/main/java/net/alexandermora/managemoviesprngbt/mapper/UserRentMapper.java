package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.UserRent;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface UserRentMapper
{
    List<UserRent> getDomains(List<UserRentDto> body);
}
