package com.scudata.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

import com.scudata.util.MaxHeap;

@DisplayName("MaxHeap Tests")
class MaxHeapTest {

    @Test
    @DisplayName("Empty heap has size 0")
    void emptyHeapSizeZero() {
        MaxHeap heap = new MaxHeap(5);
        assertEquals(0, heap.size());
    }

    @Test
    @DisplayName("Insert one element, size is 1")
    void insertOneElement() {
        MaxHeap heap = new MaxHeap(5);
        heap.insert(10);
        assertEquals(1, heap.size());
    }

    @Test
    @DisplayName("Insert up to maxSize, all inserted returns true")
    void insertUpToMaxSize() {
        MaxHeap heap = new MaxHeap(3);
        assertTrue(heap.insert(1));
        assertTrue(heap.insert(2));
        assertTrue(heap.insert(3));
        assertEquals(3, heap.size());
    }

    @Test
    @DisplayName("Insert smaller value when full returns false")
    void insertSmallerWhenFull() {
        MaxHeap heap = new MaxHeap(3);
        heap.insert(3);
        heap.insert(4);
        heap.insert(5);
        // 1 is smaller than all kept values, should be discarded
        assertFalse(heap.insert(1));
        assertEquals(3, heap.size());
    }

    @Test
    @DisplayName("Insert larger value when full returns true")
    void insertLargerWhenFull() {
        MaxHeap heap = new MaxHeap(3);
        heap.insert(3);
        heap.insert(4);
        heap.insert(5);
        // 10 is larger than the smallest kept value (3), should replace it
        assertTrue(heap.insert(10));
        assertEquals(3, heap.size());
    }

    @Test
    @DisplayName("toArray returns all kept elements")
    void toArrayReturnsAllElements() {
        MaxHeap heap = new MaxHeap(3);
        heap.insert(10);
        heap.insert(20);
        Object[] arr = heap.toArray();
        assertNotNull(arr);
        assertEquals(2, arr.length);
        Set<Object> values = new HashSet<>(Arrays.asList(arr));
        assertTrue(values.contains(10));
        assertTrue(values.contains(20));
    }

    @Test
    @DisplayName("Insert 1,3,5,2,4 with maxSize=3 keeps 3,4,5")
    void insertSequenceKeepsLargest() {
        MaxHeap heap = new MaxHeap(3);
        heap.insert(1);
        heap.insert(3);
        heap.insert(5);
        heap.insert(2);
        heap.insert(4);
        assertEquals(3, heap.size());
        Object[] arr = heap.toArray();
        Set<Object> values = new HashSet<>(Arrays.asList(arr));
        assertTrue(values.contains(3));
        assertTrue(values.contains(4));
        assertTrue(values.contains(5));
        assertFalse(values.contains(1));
        assertFalse(values.contains(2));
    }

    @Test
    @DisplayName("Size 1 heap works correctly")
    void sizeOneHeap() {
        MaxHeap heap = new MaxHeap(1);
        assertTrue(heap.insert(5));
        assertEquals(1, heap.size());
        // Insert larger value, should replace
        assertTrue(heap.insert(10));
        assertEquals(1, heap.size());
        // Insert smaller value, should be discarded
        assertFalse(heap.insert(3));
        assertEquals(1, heap.size());
    }

    @Test
    @DisplayName("Single element, insert same value returns false")
    void insertSameValueWhenFull() {
        MaxHeap heap = new MaxHeap(1);
        heap.insert(5);
        // Same value: compare <= 0, should return false
        assertFalse(heap.insert(5));
        assertEquals(1, heap.size());
    }

    @Test
    @DisplayName("Multiple same values inserted when not full")
    void multipleSameValues() {
        MaxHeap heap = new MaxHeap(3);
        assertTrue(heap.insert(5));
        assertTrue(heap.insert(5));
        assertTrue(heap.insert(5));
        assertEquals(3, heap.size());
        // All are 5, inserting another 5 should return false (not strictly greater)
        assertFalse(heap.insert(5));
        assertEquals(3, heap.size());
    }
}
