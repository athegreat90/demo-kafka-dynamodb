package net.alexandermora.managemoviesprngbt.mapper;

import net.alexandermora.managemoviesprngbt.dto.UserLikeDto;
import net.alexandermora.managemoviesprngbt.dto.UserOrderDto;
import net.alexandermora.managemoviesprngbt.dto.UserRentDto;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperTest
{
    @Test
    void givenUserBuyOrder_AssertingEqualMapper_thenEquals()
    {
        var element = new UserOrderDto();
        element.setCount(1.0D);
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        var response = UserOrderMapper.INSTANCE.getOrders(body);

        assertAll(() ->
        {
            assertEquals(element.getMovie(), response.get(0).getMovie());
            assertEquals(element.getUsername(), response.get(0).getUsername());
            assertEquals(element.getCount(), response.get(0).getCount());
        });
    }

    @Test
    void givenUserRentOrder_AssertingEqualMapper_thenEquals()
    {
        var element = new UserRentDto();
        element.setDateBegin(DateTime.now());
        element.setDateEnd(DateTime.now().plusDays(2));
        element.setMovie("abc123");
        element.setUsername("Demo1");

        var body = List.of(element);

        var response = UserRentMapper.INSTANCE.getDomains(body);

        assertAll(() ->
        {
            assertEquals(element.getMovie(), response.get(0).getMovie());
            assertEquals(element.getUsername(), response.get(0).getUsername());
            assertEquals(element.getDateBegin(), response.get(0).getDateBegin());
            assertEquals(element.getDateEnd(), response.get(0).getDateEnd());
        });
    }

    @Test
    void givenUserLike_AssertingEqualMapper_thenEquals()
    {
        var element = new UserLikeDto();
        element.setListMovies(List.of("abc123", "aa123"));
        element.setUsername("Demo1");

        var body = List.of(element);

        var response = UserLikeMovieMapper.INSTANCE.getDomains(element);

        assertAll(() ->
        {
            assertEquals(element.getListMovies(), response.getListMovies());
            assertEquals(element.getUsername(), response.getUsername());
        });
    }
}
