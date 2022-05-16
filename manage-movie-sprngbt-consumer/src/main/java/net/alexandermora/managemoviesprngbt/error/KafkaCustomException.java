package net.alexandermora.managemoviesprngbt.error;

public class KafkaCustomException extends RuntimeException
{
    public KafkaCustomException(String message) {
        super(message);
    }
}
