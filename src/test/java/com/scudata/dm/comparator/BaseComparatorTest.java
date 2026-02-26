package com.scudata.dm.comparator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.scudata.dm.comparator.BaseComparator;

@DisplayName("BaseComparator Tests")
class BaseComparatorTest {

    @Test
    @DisplayName("Compare two integers: 1 < 2 should return negative")
    void compareSmallerIntegerFirst() {
        BaseComparator cmp = new BaseComparator();
        assertTrue(cmp.compare(1, 2) < 0);
    }

    @Test
    @DisplayName("Compare equal integers should return 0")
    void compareEqualIntegers() {
        BaseComparator cmp = new BaseComparator();
        assertEquals(0, cmp.compare(5, 5));
    }

    @Test
    @DisplayName("Compare two integers: 2 > 1 should return positive")
    void compareLargerIntegerFirst() {
        BaseComparator cmp = new BaseComparator();
        assertTrue(cmp.compare(2, 1) > 0);
    }

    @Test
    @DisplayName("Compare two strings alphabetically")
    void compareTwoStrings() {
        BaseComparator cmp = new BaseComparator();
        assertTrue(cmp.compare("apple", "banana") < 0);
        assertTrue(cmp.compare("banana", "apple") > 0);
        assertEquals(0, cmp.compare("same", "same"));
    }

    @Test
    @DisplayName("Compare null with non-null: null should be smaller (negative)")
    void compareNullWithNonNull() {
        BaseComparator cmp = new BaseComparator();
        assertTrue(cmp.compare(null, 1) < 0);
    }

    @Test
    @DisplayName("Compare non-null with null: should return positive")
    void compareNonNullWithNull() {
        BaseComparator cmp = new BaseComparator();
        assertTrue(cmp.compare(1, null) > 0);
    }

    @Test
    @DisplayName("Compare null with null should return 0")
    void compareNullWithNull() {
        BaseComparator cmp = new BaseComparator();
        assertEquals(0, cmp.compare(null, null));
    }

    @Test
    @DisplayName("Default constructor works with throwExcept true")
    void defaultConstructorWorks() {
        BaseComparator cmp = new BaseComparator();
        assertNotNull(cmp);
        // Should function correctly for compatible types
        assertEquals(0, cmp.compare(42, 42));
    }
}
