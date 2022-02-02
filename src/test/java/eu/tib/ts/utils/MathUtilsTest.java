package eu.tib.ts.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilsTest {
    @Test
    void testDivideByZero_shouldReturnZero() {
        assertEquals(0, MathUtils.divide(10, 0), 0);
    }

    @Test
    void testDivideByNonZero_shouldReturnOne() {
        assertEquals(1, MathUtils.divide(10, 10), 0);
    }

    @Test
    void testPercentTotalIsZero_shouldReturnZero() {
        assertEquals(0, MathUtils.percent(10, 0), 0);
    }

    @Test
    void testPercentTotalIsNotZero_shouldReturnShare() {
        assertEquals(20, MathUtils.percent(10, 50), 0);
    }
}
