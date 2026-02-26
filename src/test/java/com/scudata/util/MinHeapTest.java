package com.scudata.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

import com.scudata.util.MinHeap;

@DisplayName("MinHeap Tests")
class MinHeapTest {

    @Test
    @DisplayName("Empty heap has size 0")
    void emptyHeapSizeZero() {
        MinHeap heap = new MinHeap(5);
        assertEquals(0, heap.size());
    }

    @Test
    @DisplayName("Insert one element, size is 1")
    void insertOneElement() {
        MinHeap heap = new MinHeap(5);
        heap.insert(10);
        assertEquals(1, heap.size());
    }

    @Test
    @DisplayName("Insert up to maxSize, all inserted returns true")
    void insertUpToMaxSize() {
        MinHeap heap = new MinHeap(3);
        assertTrue(heap.insert(3));
        assertTrue(heap.insert(1));
        assertTrue(heap.insert(2));
        assertEquals(3, heap.size());
    }

    @Test
    @DisplayName("Insert larger value than all existing when full returns false")
    void insertLargerWhenFull() {
        MinHeap heap = new MinHeap(3);
        heap.insert(1);
        heap.insert(2);
        heap.insert(3);
        // 10 is larger than all kept values, should be discarded
        assertFalse(heap.insert(10));
        assertEquals(3, heap.size());
    }

    @Test
    @DisplayName("Insert smaller value when full returns true and replaces largest")
    void insertSmallerWhenFull() {
        MinHeap heap = new MinHeap(3);
        heap.insert(3);
        heap.insert(5);
        heap.insert(7);
        // 1 is smaller than the largest kept value (7), should replace it
        assertTrue(heap.insert(1));
        assertEquals(3, heap.size());
    }

    @Test
    @DisplayName("getTop returns the largest of the kept smallest values")
    void getTopReturnsLargestKept() {
        MinHeap heap = new MinHeap(3);
        heap.insert(1);
        heap.insert(2);
        heap.insert(3);
        // Top of a min-heap (keeping smallest) is the largest among kept
        assertEquals(3, heap.getTop());
    }

    @Test
    @DisplayName("toArray returns all kept elements")
    void toArrayReturnsAllElements() {
        MinHeap heap = new MinHeap(3);
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
    @DisplayName("toArray length equals size")
    void toArrayLengthEqualsSize() {
        MinHeap heap = new MinHeap(5);
        heap.insert(1);
        heap.insert(2);
        heap.insert(3);
        assertEquals(heap.size(), heap.toArray().length);
    }

    @Test
    @DisplayName("Insert sequence 5,3,1,4,2 with maxSize=3 keeps 1,2,3")
    void insertSequenceKeepsSmallest() {
        MinHeap heap = new MinHeap(3);
        heap.insert(5);
        heap.insert(3);
        heap.insert(1);
        heap.insert(4);
        heap.insert(2);
        assertEquals(3, heap.size());
        Object[] arr = heap.toArray();
        Set<Object> values = new HashSet<>(Arrays.asList(arr));
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertTrue(values.contains(3));
        assertFalse(values.contains(4));
        assertFalse(values.contains(5));
    }

    @Test
    @DisplayName("insertAll merges two heaps correctly")
    void insertAllMergesHeaps() {
        MinHeap heap1 = new MinHeap(3);
        heap1.insert(1);
        heap1.insert(3);
        heap1.insert(5);

        MinHeap heap2 = new MinHeap(3);
        heap2.insert(2);
        heap2.insert(4);

        heap1.insertAll(heap2);
        // After merge, heap1 should keep the 3 smallest: 1, 2, 3
        assertEquals(3, heap1.size());
        Object[] arr = heap1.toArray();
        Set<Object> values = new HashSet<>(Arrays.asList(arr));
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertTrue(values.contains(3));
    }

    @Test
    @DisplayName("Size 1 heap replaces correctly")
    void sizeOneHeap() {
        MinHeap heap = new MinHeap(1);
        assertTrue(heap.insert(5));
        assertEquals(1, heap.size());
        assertEquals(5, heap.getTop());
        // Insert smaller value, should replace
        assertTrue(heap.insert(2));
        assertEquals(1, heap.size());
        assertEquals(2, heap.getTop());
        // Insert larger value, should be discarded
        assertFalse(heap.insert(10));
        assertEquals(2, heap.getTop());
    }

    @Test
    @DisplayName("Null is treated as smallest value")
    void nullIsTreatedAsSmallest() {
        MinHeap heap = new MinHeap(3);
        heap.insert(1);
        heap.insert(2);
        heap.insert(3);
        // null is smaller than everything, should replace the largest (3)
        assertTrue(heap.insert(null));
        assertEquals(3, heap.size());
        Object[] arr = heap.toArray();
        Set<Object> values = new HashSet<>(Arrays.asList(arr));
        assertTrue(values.contains(null));
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
    }
}
