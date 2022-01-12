package eu.tib.ts.utils;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionUtilsTest {
    @Test
    void testIntersection() {
        Set<Integer> set1 = Set.of(1, 2, 3, 4, 8, 9);
        Set<Integer> set2 = Set.of(1, 3, 8, 5, 11);
        Set<Integer> set3 = Set.of(1, 3, 7, 8, 20);

        Set<Integer> actual = CollectionUtils.intersection(List.of(set1, set2, set3));
        Set<Integer> expected = Set.of(1, 3, 8);

        assertEquals(expected, actual);
    }

    @Test
    void testIntersection_oneSetIsEmpty() {
        Set<Integer> set1 = Set.of(1, 2, 3, 4, 8, 9);
        Set<Integer> set2 = Set.of(1, 3, 8, 5, 11);
        Set<Integer> set3 = Collections.emptySet();

        Set<Integer> actual = CollectionUtils.intersection(List.of(set1, set2, set3));
        Set<Integer> expected = Collections.emptySet();

        assertEquals(expected, actual);
    }

    @Test
    void testIntersection_listOfSetsIsEmpty() {
        Set<Integer> actual = CollectionUtils.intersection(Collections.emptyList());
        Set<Integer> expected = Collections.emptySet();

        assertEquals(expected, actual);
    }
}
