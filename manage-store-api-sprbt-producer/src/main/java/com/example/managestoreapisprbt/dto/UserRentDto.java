package com.example.managestoreapisprbt.dto;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class UserRentDto
{
    private String id;

    private DateTime dateBegin;

    private DateTime dateEnd;

    private String username;

    private String movie;
}
