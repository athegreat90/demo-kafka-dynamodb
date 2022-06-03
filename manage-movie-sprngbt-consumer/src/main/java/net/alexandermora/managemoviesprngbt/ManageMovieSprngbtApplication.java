package net.alexandermora.managemoviesprngbt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"net.alexandermora.managemoviesprngbt", "net.alexandermora.managemoviesprngbt.consumer", "net.alexandermora.managemoviesprngbt.mapper", "net.alexandermora.managemoviesprngbt.error"})
@SpringBootApplication
public class ManageMovieSprngbtApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageMovieSprngbtApplication.class, args);
    }

}
