package com.scudata.dm.comparator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.scudata.dm.comparator.ArrayComparator;

@DisplayName("ArrayComparator Tests")
class ArrayComparatorTest {

    @Test
    @DisplayName("Equal arrays should return 0")
    void compareEqualArrays() {
        ArrayComparator cmp = new ArrayComparator(3);
        Object[] a = {1, 2, 3};
        Object[] b = {1, 2, 3};
        assertEquals(0, cmp.compare(a, b));
    }

    @Test
    @DisplayName("First element differs should return correct sign")
    void compareFirstElementDiffers() {
        ArrayComparator cmp = new ArrayComparator(3);
        Object[] a = {1, 2, 3};
        Object[] b = {2, 2, 3};
        assertTrue(cmp.compare(a, b) < 0);
        assertTrue(cmp.compare(b, a) > 0);
    }

    @Test
    @DisplayName("Second element differs when first is equal")
    void compareSecondElementDiffers() {
        ArrayComparator cmp = new ArrayComparator(3);
        Object[] a = {1, 2, 3};
        Object[] b = {1, 5, 3};
        assertTrue(cmp.compare(a, b) < 0);
    }

    @Test
    @DisplayName("Null element vs non-null element: null is smaller")
    void compareNullElementVsNonNull() {
        ArrayComparator cmp = new ArrayComparator(2);
        Object[] a = {null, 2};
        Object[] b = {1, 2};
        assertTrue(cmp.compare(a, b) < 0);
    }

    @Test
    @DisplayName("Both null elements should return 0")
    void compareBothNullElements() {
        ArrayComparator cmp = new ArrayComparator(2);
        Object[] a = {null, null};
        Object[] b = {null, null};
        assertEquals(0, cmp.compare(a, b));
    }

    @Test
    @DisplayName("Length 1 comparison works correctly")
    void compareLengthOne() {
        ArrayComparator cmp = new ArrayComparator(1);
        Object[] a = {10};
        Object[] b = {20};
        assertTrue(cmp.compare(a, b) < 0);
        assertTrue(cmp.compare(b, a) > 0);
        assertEquals(0, cmp.compare(new Object[]{10}, new Object[]{10}));
    }

    @Test
    @DisplayName("Only first len elements are compared in longer arrays")
    void compareOnlyFirstLenElements() {
        ArrayComparator cmp = new ArrayComparator(2);
        Object[] a = {1, 2, 100};
        Object[] b = {1, 2, 999};
        // Only first 2 elements compared, so arrays are equal
        assertEquals(0, cmp.compare(a, b));
    }
}
