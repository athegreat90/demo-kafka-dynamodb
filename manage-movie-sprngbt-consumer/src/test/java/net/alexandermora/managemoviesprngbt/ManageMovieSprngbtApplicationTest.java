package net.alexandermora.managemoviesprngbt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManageMovieSprngbtApplicationTest {

    @Test
    void main() {
        var args = new String[1];
        args[0] = "de";
        assertDoesNotThrow(() -> ManageMovieSprngbtApplication.main(args));
    }
}