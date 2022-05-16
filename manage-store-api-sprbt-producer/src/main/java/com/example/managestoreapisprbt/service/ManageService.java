package com.example.managestoreapisprbt.service;

import com.example.managestoreapisprbt.dto.UserLikeDto;
import com.example.managestoreapisprbt.dto.UserOrderDto;
import com.example.managestoreapisprbt.dto.UserRentDto;

import java.util.List;

public interface ManageService
{
    void sendBuyEvent(List<UserOrderDto> orderDto);

    void sendLikeEvent(List<UserLikeDto> likeDto);

    void sendRentEvent(List<UserRentDto> rentDto);
}
