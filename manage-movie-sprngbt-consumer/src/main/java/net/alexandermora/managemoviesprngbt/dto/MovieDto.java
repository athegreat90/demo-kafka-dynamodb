package net.alexandermora.managemoviesprngbt.dto;

import lombok.Data;

@Data
public class MovieDto
{
    private String id;

    private String title;

    private String description;

    private Integer stock;

    private Double rentalPrice;

    private Double salePrice;

    private Boolean availability;

    private OperationType operation;
}

