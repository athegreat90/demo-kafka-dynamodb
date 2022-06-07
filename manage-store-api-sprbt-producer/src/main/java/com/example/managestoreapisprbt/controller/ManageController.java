package com.example.managestoreapisprbt.controller;

import com.example.managestoreapisprbt.dto.UserLikeDto;
import com.example.managestoreapisprbt.dto.UserOrderDto;
import com.example.managestoreapisprbt.dto.UserRentDto;
import com.example.managestoreapisprbt.service.ManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/manage/")
public class ManageController
{
    private final ManageService manageService;

    @PostMapping("/buy")
    public void buy(@RequestBody List<UserOrderDto> body)
    {
        manageService.sendBuyEvent(body);
    }

    @PostMapping("/rent")
    public void rent(@RequestBody List<UserRentDto> body)
    {
        manageService.sendRentEvent(body);
    }

    @PostMapping("/like")
    public void like(@RequestBody List<UserLikeDto> body)
    {
        manageService.sendLikeEvent(body);
    }
}
