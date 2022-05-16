package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.domain.UserOrder;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface UserOrderMapper
{
    List<UserOrder> getOrders(List<UserOrderDto> body);
}
