package com.virusxar.physiobook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PhysioBookApplicationTests {

    @Test
    void contextLoads() {
    }
}