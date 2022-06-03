package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.UserOrder;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserOrderMapper
{
    UserOrderMapper INSTANCE = Mappers.getMapper(UserOrderMapper.class);
    List<UserOrder> getOrders(List<UserOrderDto> body);
}
