package net.alexandermora.managemoviesprngbt;

import net.alexandermora.managemoviesprngbt.error.KafkaCustomException;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;

public class ExceptionTest {

    @Test
    void givenReason_wouldReturnException()
    {
        assertNotNull(new KafkaCustomException("Demo"));
    }
}
