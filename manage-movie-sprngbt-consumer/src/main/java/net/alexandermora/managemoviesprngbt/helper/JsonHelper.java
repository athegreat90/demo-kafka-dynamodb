package net.alexandermora.managemoviesprngbt.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JsonHelper
{
    private final ObjectMapper objectMapper;

    public String getJson(Object o)
    {
        try
        {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
