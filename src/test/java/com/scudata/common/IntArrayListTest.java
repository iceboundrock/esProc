package com.scudata.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IntArrayList - primitive int array list")
public class IntArrayListTest {

    private IntArrayList list;

    @BeforeEach
    void setUp() {
        list = new IntArrayList();
    }

    @Test
    void defaultConstructorIsEmpty() {
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    void constructorWithCapacity() {
        IntArrayList l = new IntArrayList(100);
        assertEquals(100, l.capacity());
        assertEquals(0, l.size());
    }

    @Test
    void constructorNegativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IntArrayList(-1));
    }

    @Test
    void addIntAndGetInt() {
        list.addInt(42);
        list.addInt(99);
        assertEquals(2, list.size());
        assertEquals(42, list.getInt(0));
        assertEquals(99, list.getInt(1));
    }

    @Test
    void addAll() {
        list.addAll(new int[]{1, 2, 3, 4, 5});
        assertEquals(5, list.size());
        assertEquals(3, list.getInt(2));
    }

    @Test
    void addAllNull() {
        assertFalse(list.addAll(null));
    }

    @Test
    void setInt() {
        list.addInt(10);
        int old = list.setInt(0, 20);
        assertEquals(10, old);
        assertEquals(20, list.getInt(0));
    }

    @Test
    void setIntOutOfBoundsThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.setInt(0, 1));
    }

    @Test
    void containsInt() {
        list.addInt(5);
        list.addInt(10);
        assertTrue(list.containsInt(5));
        assertTrue(list.containsInt(10));
        assertFalse(list.containsInt(99));
    }

    @Test
    void indexOfInt() {
        list.addInt(10);
        list.addInt(20);
        list.addInt(10);
        assertEquals(0, list.indexOfInt(10));
        assertEquals(1, list.indexOfInt(20));
        assertEquals(-1, list.indexOfInt(99));
    }

    @Test
    void lastIndexOfInt() {
        list.addInt(10);
        list.addInt(20);
        list.addInt(10);
        assertEquals(2, list.lastIndexOfInt(10));
        assertEquals(1, list.lastIndexOfInt(20));
        assertEquals(-1, list.lastIndexOfInt(99));
    }

    @Test
    void addIntAtIndex() {
        list.addInt(1);
        list.addInt(3);
        list.addInt(1, 2); // insert 2 at index 1
        assertEquals(3, list.size());
        assertEquals(1, list.getInt(0));
        assertEquals(2, list.getInt(1));
        assertEquals(3, list.getInt(2));
    }

    @Test
    void removeIntAt() {
        list.addInt(10);
        list.addInt(20);
        list.addInt(30);
        int removed = list.removeIntAt(1);
        assertEquals(20, removed);
        assertEquals(2, list.size());
        assertEquals(30, list.getInt(1));
    }

    @Test
    void removeInt() {
        list.addInt(10);
        list.addInt(20);
        assertTrue(list.removeInt(10));
        assertFalse(list.removeInt(99));
        assertEquals(1, list.size());
    }

    @Test
    void clear() {
        list.addInt(1);
        list.addInt(2);
        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    void toIntArray() {
        list.addInt(1);
        list.addInt(2);
        list.addInt(3);
        int[] arr = list.toIntArray();
        assertArrayEquals(new int[]{1, 2, 3}, arr);
    }

    @Test
    void ensureCapacityGrows() {
        IntArrayList small = new IntArrayList(2);
        small.addInt(1);
        small.addInt(2);
        small.addInt(3); // triggers growth
        assertEquals(3, small.size());
        assertEquals(3, small.getInt(2));
    }

    @Test
    void trimToSize() {
        list.addInt(1);
        list.addInt(2);
        list.trimToSize();
        assertEquals(2, list.capacity());
    }

    @Test
    void binarySearchFound() {
        list.addAll(new int[]{10, 20, 30, 40, 50});
        assertEquals(2, list.binarySearch(30));
    }

    @Test
    void binarySearchNotFound() {
        list.addAll(new int[]{10, 20, 30, 40, 50});
        int result = list.binarySearch(25);
        assertTrue(result < 0);
        // insertion point should be 2 (between 20 and 30), so result = -(2+1) = -3
        assertEquals(-3, result);
    }

    @Test
    void binarySearchWithRange() {
        list.addAll(new int[]{10, 20, 30, 40, 50});
        assertEquals(3, list.binarySearch(40, 1, 4));
    }

    @Test
    void getBoxedInteger() {
        list.addInt(42);
        Integer val = list.get(0);
        assertEquals(42, val.intValue());
    }

    @Test
    void containsBoxed() {
        list.addInt(42);
        assertTrue(list.contains(Integer.valueOf(42)));
    }

    @Test
    void setSizeDirectly() {
        list.addInt(1);
        list.addInt(2);
        list.addInt(3);
        list.setSize(2);
        assertEquals(2, list.size());
    }

    @Test
    void removeByIndex() {
        list.addInt(10);
        list.addInt(20);
        Integer removed = list.remove(0);
        assertEquals(10, removed.intValue());
        assertEquals(1, list.size());
    }

    @Test
    void removeByObject() {
        list.addInt(10);
        list.addInt(20);
        assertTrue(list.remove(Integer.valueOf(10)));
        assertFalse(list.remove(Integer.valueOf(99)));
    }
}
