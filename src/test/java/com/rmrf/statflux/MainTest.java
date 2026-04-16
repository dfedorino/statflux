package com.rmrf.statflux;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void smoke() {
        assertThatCode(() -> Main.main(null))
            .doesNotThrowAnyException();
        Assertions.fail();
    }
}