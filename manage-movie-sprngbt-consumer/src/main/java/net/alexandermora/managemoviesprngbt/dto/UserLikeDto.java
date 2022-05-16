package net.alexandermora.managemoviesprngbt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class UserLikeDto
{
    private String id;
    private String username;
    private List<String> listMovies;
}
