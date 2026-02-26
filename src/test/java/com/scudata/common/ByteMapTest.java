package com.scudata.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ByteMap Tests")
class ByteMapTest {

    private ByteMap map;

    @BeforeEach
    void setUp() {
        map = new ByteMap();
    }

    // ---- Constructor tests ----

    @Test
    @DisplayName("Default constructor creates empty map with size 0")
    void defaultConstructorCreatesEmptyMap() {
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    @DisplayName("Custom capacity constructor creates empty map")
    void customCapacityConstructor() {
        ByteMap custom = new ByteMap((short) 50);
        assertEquals(0, custom.size());
        assertTrue(custom.isEmpty());
    }

    @Test
    @DisplayName("Negative capacity throws IllegalArgumentException")
    void negativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ByteMap((short) -1));
    }

    // ---- put / get tests ----

    @Test
    @DisplayName("put new key returns null")
    void putNewKeyReturnsNull() {
        Object result = map.put((byte) 1, "alpha");
        assertNull(result);
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("put existing key returns old value and overwrites")
    void putExistingKeyReturnsOldValue() {
        map.put((byte) 1, "alpha");
        Object old = map.put((byte) 1, "beta");
        assertEquals("alpha", old);
        assertEquals("beta", map.get((byte) 1));
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("get existing key returns correct value")
    void getExistingKey() {
        map.put((byte) 5, "five");
        assertEquals("five", map.get((byte) 5));
    }

    @Test
    @DisplayName("get missing key returns null")
    void getMissingKeyReturnsNull() {
        assertNull(map.get((byte) 99));
    }

    // ---- add tests ----

    @Test
    @DisplayName("add appends without overwriting and can create duplicates")
    void addAppendsDuplicates() {
        map.add((byte) 1, "first");
        map.add((byte) 1, "second");
        assertEquals(2, map.size());
        // get searches from the end, so it returns the last added value
        assertEquals("second", map.get((byte) 1));
        // but both entries exist at index 0 and 1
        assertEquals("first", map.getValue(0));
        assertEquals("second", map.getValue(1));
    }

    // ---- containsKey / contains tests ----

    @Test
    @DisplayName("containsKey returns true for present key, false for absent")
    void containsKeyTrueFalse() {
        map.put((byte) 10, "ten");
        assertTrue(map.containsKey((byte) 10));
        assertFalse(map.containsKey((byte) 20));
    }

    @Test
    @DisplayName("contains returns true for present value, false for absent")
    void containsValueTrueFalse() {
        map.put((byte) 1, "hello");
        assertTrue(map.contains("hello"));
        assertFalse(map.contains("world"));
    }

    @Test
    @DisplayName("contains(null) always returns false")
    void containsNullReturnsFalse() {
        map.put((byte) 1, null);
        assertFalse(map.contains(null));
    }

    // ---- remove tests ----

    @Test
    @DisplayName("remove existing key returns value and decreases size")
    void removeExistingKey() {
        map.put((byte) 1, "alpha");
        map.put((byte) 2, "beta");
        Object removed = map.remove((byte) 1);
        assertEquals("alpha", removed);
        assertEquals(1, map.size());
        assertNull(map.get((byte) 1));
    }

    @Test
    @DisplayName("remove missing key returns null")
    void removeMissingKeyReturnsNull() {
        assertNull(map.remove((byte) 42));
    }

    @Test
    @DisplayName("removeEntry by index returns value and shifts elements")
    void removeEntryByIndex() {
        map.put((byte) 1, "a");
        map.put((byte) 2, "b");
        map.put((byte) 3, "c");
        Object removed = map.removeEntry(1); // remove key=2
        assertEquals("b", removed);
        assertEquals(2, map.size());
        // remaining: key=1->"a", key=3->"c"
        assertEquals((byte) 1, map.getKey(0));
        assertEquals((byte) 3, map.getKey(1));
    }

    // ---- size / isEmpty / clear tests ----

    @Test
    @DisplayName("size and isEmpty reflect entries correctly")
    void sizeAndIsEmpty() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        map.put((byte) 1, "one");
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        map.put((byte) 2, "two");
        assertEquals(2, map.size());
    }

    @Test
    @DisplayName("clear resets size to 0")
    void clearResetsSize() {
        map.put((byte) 1, "a");
        map.put((byte) 2, "b");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get((byte) 1));
    }

    // ---- ensureCapacity / trimToSize tests ----

    @Test
    @DisplayName("ensureCapacity grows internal array without losing data")
    void ensureCapacityGrows() {
        map.put((byte) 1, "a");
        map.put((byte) 2, "b");
        map.ensureCapacity(100);
        // data is preserved
        assertEquals(2, map.size());
        assertEquals("a", map.get((byte) 1));
        assertEquals("b", map.get((byte) 2));
        // internal keys array should be at least 100
        assertTrue(map.getKeys().length >= 100);
    }

    @Test
    @DisplayName("trimToSize shrinks internal array to actual size")
    void trimToSizeShrinks() {
        map.put((byte) 1, "a");
        map.put((byte) 2, "b");
        // default capacity is 11, so keys.length > count
        assertTrue(map.getKeys().length > map.size());
        map.trimToSize();
        assertEquals(2, map.getKeys().length);
        // data preserved
        assertEquals("a", map.get((byte) 1));
        assertEquals("b", map.get((byte) 2));
    }

    // ---- getKey / getValue / getIndex / setValue tests ----

    @Test
    @DisplayName("getKey, getValue, getIndex, and setValue by index")
    void indexBasedAccessors() {
        map.put((byte) 10, "ten");
        map.put((byte) 20, "twenty");

        assertEquals((byte) 10, map.getKey(0));
        assertEquals("ten", map.getValue(0));
        assertEquals((byte) 20, map.getKey(1));
        assertEquals("twenty", map.getValue(1));

        assertEquals(0, map.getIndex((byte) 10));
        assertEquals(1, map.getIndex((byte) 20));
        assertEquals(-1, map.getIndex((byte) 99));

        map.setValue(0, "TEN");
        assertEquals("TEN", map.getValue(0));
        assertEquals("TEN", map.get((byte) 10));
    }

    // ---- purgeDupKeys tests ----

    @Test
    @DisplayName("purgeDupKeys removes duplicates keeping last added value")
    void purgeDupKeepsLast() {
        map.add((byte) 1, "first");
        map.add((byte) 2, "two");
        map.add((byte) 1, "second");
        map.add((byte) 1, "third");
        assertEquals(4, map.size());

        map.purgeDupKeys();
        assertEquals(2, map.size());
        // last value for key 1 should be "third"
        assertEquals("third", map.get((byte) 1));
        assertEquals("two", map.get((byte) 2));
    }

    // ---- purgeNullValues tests ----

    @Test
    @DisplayName("purgeNullValues removes entries with null values")
    void purgeNullValuesRemovesNulls() {
        map.add((byte) 1, "abc");
        map.add((byte) 2, null);
        map.add((byte) 3, "def");
        map.add((byte) 4, null);
        assertEquals(4, map.size());

        map.purgeNullValues();
        assertEquals(2, map.size());
        assertTrue(map.containsKey((byte) 1));
        assertTrue(map.containsKey((byte) 3));
        assertFalse(map.containsKey((byte) 2));
        assertFalse(map.containsKey((byte) 4));
    }

    // ---- deepClone tests ----

    @Test
    @DisplayName("deepClone produces an independent copy")
    void deepCloneIsIndependent() {
        map.put((byte) 1, "one");
        map.put((byte) 2, "two");

        ByteMap clone = (ByteMap) map.deepClone();
        assertEquals(map.size(), clone.size());
        assertEquals(map.get((byte) 1), clone.get((byte) 1));
        assertEquals(map.get((byte) 2), clone.get((byte) 2));

        // modifying clone does not affect original
        clone.put((byte) 1, "modified");
        assertEquals("one", map.get((byte) 1));
        assertEquals("modified", clone.get((byte) 1));

        clone.put((byte) 3, "three");
        assertEquals(2, map.size());
        assertEquals(3, clone.size());
    }

    // ---- toString tests ----

    @Test
    @DisplayName("toString produces correct format")
    void toStringFormat() {
        assertEquals("{}", map.toString());

        map.put((byte) 1, "a");
        assertEquals("{1=a}", map.toString());

        map.put((byte) 2, "b");
        assertEquals("{1=a, 2=b}", map.toString());
    }

    // ---- auto-grow tests ----

    @Test
    @DisplayName("put auto-grows when capacity is full")
    void putAutoGrowsWhenFull() {
        ByteMap small = new ByteMap((short) 2);
        small.put((byte) 1, "a");
        small.put((byte) 2, "b");
        // this should trigger auto-grow
        Object result = small.put((byte) 3, "c");
        assertNull(result);
        assertEquals(3, small.size());
        assertEquals("a", small.get((byte) 1));
        assertEquals("b", small.get((byte) 2));
        assertEquals("c", small.get((byte) 3));
    }

    // ---- putAll / addAll tests ----

    @Test
    @DisplayName("putAll merges another ByteMap, overwriting duplicate keys")
    void putAllOverwritesDuplicates() {
        map.put((byte) 1, "original");
        map.put((byte) 2, "two");

        ByteMap other = new ByteMap();
        other.put((byte) 1, "overwritten");
        other.put((byte) 3, "three");

        map.putAll(other);
        assertEquals(3, map.size());
        assertEquals("overwritten", map.get((byte) 1));
        assertEquals("two", map.get((byte) 2));
        assertEquals("three", map.get((byte) 3));
    }

    @Test
    @DisplayName("addAll appends from another ByteMap without overwriting")
    void addAllAppendsDuplicates() {
        map.put((byte) 1, "original");

        ByteMap other = new ByteMap();
        other.put((byte) 1, "duplicate");
        other.put((byte) 2, "two");

        map.addAll(other);
        // key 1 appears twice now
        assertEquals(3, map.size());
        // get returns the last added (from addAll), since get searches from end
        assertEquals("duplicate", map.get((byte) 1));
        assertEquals("two", map.get((byte) 2));
    }

    // ---- getKeys tests ----

    @Test
    @DisplayName("getKeys returns internal keys array")
    void getKeysReturnsInternalArray() {
        map.put((byte) 5, "five");
        map.put((byte) 10, "ten");
        byte[] keys = map.getKeys();
        assertNotNull(keys);
        // default capacity is 11, so length is 11
        assertEquals(11, keys.length);
        assertEquals((byte) 5, keys[0]);
        assertEquals((byte) 10, keys[1]);
    }

    // ---- zero-capacity constructor edge case ----

    @Test
    @DisplayName("Zero capacity constructor works and auto-grows on put")
    void zeroCapacityConstructor() {
        ByteMap empty = new ByteMap((short) 0);
        assertEquals(0, empty.size());
        // put should trigger auto-grow from 0 capacity
        empty.put((byte) 1, "first");
        assertEquals(1, empty.size());
        assertEquals("first", empty.get((byte) 1));
    }

    // ---- add auto-grow tests ----

    @Test
    @DisplayName("add auto-grows when capacity is full")
    void addAutoGrowsWhenFull() {
        ByteMap small = new ByteMap((short) 1);
        small.add((byte) 1, "a");
        // this triggers auto-grow
        small.add((byte) 2, "b");
        assertEquals(2, small.size());
        assertEquals("a", small.get((byte) 1));
        assertEquals("b", small.get((byte) 2));
    }
}
