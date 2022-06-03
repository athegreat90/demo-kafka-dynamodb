package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.UserRent;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserRentMapper
{
    UserRentMapper INSTANCE = Mappers.getMapper(UserRentMapper.class);
    List<UserRent> getDomains(List<UserRentDto> body);
}
