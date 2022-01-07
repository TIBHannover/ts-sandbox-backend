package eu.tib.ts.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageUtilsTest {
    @Test
    void testPageUtils() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        PageRequest pageRequest = PageRequest.of(0, 5);
        PageImpl<Integer> actual = PageUtils.toPage(list, pageRequest);

        assertEquals(2, actual.getTotalPages());
        assertEquals(10, actual.getTotalElements());
        assertEquals(list.subList(0, 5), actual.getContent());
    }
}
