package com.itranswarp.exchange.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JDK17ExampleTest {

    @Test
    void qq() {
        JDK17Example example = new JDK17Example();
//        System.out.println(example.qqId());
        Assertions.assertEquals(10L, example.qqId());
    }
}