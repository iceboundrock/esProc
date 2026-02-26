package com.scudata.dm.comparator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.*;

import com.scudata.dm.comparator.DescComparator;
import com.scudata.dm.comparator.BaseComparator;

@DisplayName("DescComparator Tests")
class DescComparatorTest {

    @Test
    @DisplayName("Default constructor reverses integer comparison")
    void defaultConstructorReversesComparison() {
        DescComparator cmp = new DescComparator();
        // 1 vs 2: normally negative, reversed should be positive
        assertTrue(cmp.compare(1, 2) > 0);
    }

    @Test
    @DisplayName("1 vs 2 returns positive (reversed)")
    void compareOneVsTwo() {
        DescComparator cmp = new DescComparator();
        assertTrue(cmp.compare(1, 2) > 0);
    }

    @Test
    @DisplayName("2 vs 1 returns negative (reversed)")
    void compareTwoVsOne() {
        DescComparator cmp = new DescComparator();
        assertTrue(cmp.compare(2, 1) < 0);
    }

    @Test
    @DisplayName("Equal values return 0")
    void compareEqualValues() {
        DescComparator cmp = new DescComparator();
        assertEquals(0, cmp.compare(5, 5));
    }

    @Test
    @DisplayName("Null handling is reversed: null vs non-null returns positive")
    void nullHandlingReversed() {
        DescComparator cmp = new DescComparator();
        // Normally null < non-null (negative), reversed should be positive
        assertTrue(cmp.compare(null, 1) > 0);
        // Normally non-null > null (positive), reversed should be negative
        assertTrue(cmp.compare(1, null) < 0);
    }

    @Test
    @DisplayName("deepClone creates an independent copy")
    void deepCloneCreatesIndependentCopy() {
        DescComparator original = new DescComparator();
        DescComparator cloned = (DescComparator) original.deepClone();
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        // Cloned should behave the same
        assertEquals(original.compare(1, 2), cloned.compare(1, 2));
        assertEquals(original.compare(5, 5), cloned.compare(5, 5));
    }

    @Test
    @DisplayName("deepClone with non-ICloneable comparator")
    void deepCloneWithNonCloneableComparator() {
        Comparator<Object> simple = (a, b) -> {
            int ia = ((Integer) a);
            int ib = ((Integer) b);
            return Integer.compare(ia, ib);
        };
        DescComparator original = new DescComparator(simple);
        DescComparator cloned = (DescComparator) original.deepClone();
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        // Should still reverse comparison
        assertTrue(cloned.compare(1, 2) > 0);
    }

    @Test
    @DisplayName("Custom comparator wrapping reverses correctly")
    void customComparatorWrapping() {
        BaseComparator base = new BaseComparator();
        DescComparator cmp = new DescComparator(base);
        // Reversed: "apple" vs "banana" normally negative, should be positive
        assertTrue(cmp.compare("apple", "banana") > 0);
        assertTrue(cmp.compare("banana", "apple") < 0);
    }
}
