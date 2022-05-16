package com.example.managestoreapisprbt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ManageStoreApiSprbtApplicationTests {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> ManageStoreApiSprbtApplication.main(new String[]{}));

    }

}
