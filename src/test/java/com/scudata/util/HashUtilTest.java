package com.scudata.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.scudata.dm.Env;

@DisplayName("HashUtil Tests")
class HashUtilTest {

    @Test
    @DisplayName("Default constructor uses Env.getDefaultHashCapacity()")
    void defaultConstructorUsesEnvCapacity() {
        HashUtil hu = new HashUtil();
        assertEquals(Env.getDefaultHashCapacity(), hu.getCapacity());
    }

    @Test
    @DisplayName("Constructor with capacity adjusts to nearest prime")
    void constructorWithCapacityAdjustsToPrime() {
        // 50 is between primes 41 and 59 in the PRIMES table; should round up to 59
        HashUtil hu = new HashUtil(50);
        assertEquals(59, hu.getCapacity());

        // Exact match: passing an exact prime should return that prime
        HashUtil huExact = new HashUtil(29);
        assertEquals(29, huExact.getCapacity());

        // Very small capacity should go to the first prime (13)
        HashUtil huSmall = new HashUtil(1);
        assertEquals(13, huSmall.getCapacity());
    }

    @Test
    @DisplayName("Constructor with doAdjust=false uses capacity as-is")
    void constructorNoAdjust() {
        HashUtil hu = new HashUtil(100, false);
        assertEquals(100, hu.getCapacity());

        // An arbitrary non-prime value
        HashUtil hu2 = new HashUtil(42, false);
        assertEquals(42, hu2.getCapacity());
    }

    @Test
    @DisplayName("Constructor with doAdjust=true adjusts to prime")
    void constructorDoAdjustTrue() {
        HashUtil hu = new HashUtil(50, true);
        assertEquals(59, hu.getCapacity());
    }

    @Test
    @DisplayName("getCapacity returns adjusted prime capacity")
    void getCapacityReturnsPrime() {
        // 200 is between 197 and 263
        HashUtil hu = new HashUtil(200);
        int cap = hu.getCapacity();
        assertEquals(263, cap);
    }

    @Test
    @DisplayName("hashCode(int) returns non-negative value")
    void hashCodeIntNonNegative() {
        HashUtil hu = new HashUtil(100, false);

        // Positive input
        assertTrue(hu.hashCode(42) >= 0);
        // Zero input
        assertTrue(hu.hashCode(0) >= 0);
        // Negative input — should still be non-negative
        assertTrue(hu.hashCode(-12345) >= 0);
        assertTrue(hu.hashCode(Integer.MIN_VALUE) >= 0);

        // Verify the result is less than capacity
        assertTrue(hu.hashCode(42) < 100);
        assertTrue(hu.hashCode(-999) < 100);
    }

    @Test
    @DisplayName("hashCode(Object) returns 0 for null, non-negative for non-null")
    void hashCodeObjectNullAndNonNull() {
        HashUtil hu = new HashUtil(59);

        // null returns 0
        assertEquals(0, hu.hashCode((Object) null));

        // Non-null string
        int h = hu.hashCode((Object) "hello");
        assertTrue(h >= 0);
        assertTrue(h < hu.getCapacity());

        // Non-null integer
        int h2 = hu.hashCode((Object) Integer.valueOf(42));
        assertTrue(h2 >= 0);
        assertTrue(h2 < hu.getCapacity());
    }

    @Test
    @DisplayName("hashCode(Object[]) combines array element hashes")
    void hashCodeObjectArray() {
        HashUtil hu = new HashUtil(107);

        Object[] vals = {"a", "b", "c"};
        int h = hu.hashCode(vals);
        assertTrue(h >= 0);
        assertTrue(h < hu.getCapacity());

        // Single element array
        Object[] single = {"test"};
        int hSingle = hu.hashCode(single);
        assertTrue(hSingle >= 0);

        // Array with null elements
        Object[] withNulls = {"x", null, "y"};
        int hNulls = hu.hashCode(withNulls);
        assertTrue(hNulls >= 0);
        assertTrue(hNulls < hu.getCapacity());
    }

    @Test
    @DisplayName("hashCode(Object[], count) uses only first count elements")
    void hashCodeObjectArrayWithCount() {
        HashUtil hu = new HashUtil(107);

        Object[] vals = {"a", "b", "c", "d"};
        int hFull = hu.hashCode(vals, 4);
        int hPartial = hu.hashCode(vals, 2);

        // Both should be non-negative
        assertTrue(hFull >= 0);
        assertTrue(hPartial >= 0);
        assertTrue(hFull < hu.getCapacity());
        assertTrue(hPartial < hu.getCapacity());

        // Using count=full length should equal hashCode(Object[])
        assertEquals(hFull, hu.hashCode(vals));

        // Using count=2 should differ from full array (in general)
        // (could coincidentally match, but with these values it won't)
        // Instead, verify consistency: same input, same output
        assertEquals(hPartial, hu.hashCode(vals, 2));

        // count=1 should behave like hashCode(Object) for that single element
        int hOne = hu.hashCode(vals, 1);
        int hDirect = hu.hashCode((Object) "a");
        assertEquals(hDirect, hOne);

        // Array with null in first position
        Object[] nullFirst = {null, "b"};
        int hNull = hu.hashCode(nullFirst, 2);
        assertTrue(hNull >= 0);
    }

    @Test
    @DisplayName("Static hashCode(long) computes XOR of upper and lower 32 bits")
    void hashCodeLong() {
        // For value fitting in int, upper 32 bits are zero
        assertEquals(42, HashUtil.hashCode(42L));
        assertEquals(0, HashUtil.hashCode(0L));

        // Known computation: value = 0x00000001_00000001L
        // (1 ^ 1) = 0
        long val = (1L << 32) | 1L;
        assertEquals(0, HashUtil.hashCode(val));

        // value = 0x00000002_00000003L => 2 ^ 3 = 1
        long val2 = (2L << 32) | 3L;
        assertEquals(1, HashUtil.hashCode(val2));

        // Negative value
        // -1L = 0xFFFFFFFF_FFFFFFFF => (0xFFFFFFFF ^ 0xFFFFFFFF) = 0
        assertEquals(0, HashUtil.hashCode(-1L));

        // Long.MAX_VALUE = 0x7FFFFFFF_FFFFFFFF
        // upper = 0x7FFFFFFF, lower = 0xFFFFFFFF (as int = -1)
        // result = 0x7FFFFFFF ^ (-1) = 0x80000000 = Integer.MIN_VALUE
        assertEquals(Integer.MIN_VALUE, HashUtil.hashCode(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Static hashCode(Object, int) returns non-negative and 0 for null")
    void hashCodeStaticObjectCapacity() {
        // null returns 0
        assertEquals(0, HashUtil.hashCode((Object) null, 100));

        // Non-null
        int h = HashUtil.hashCode("test", 100);
        assertTrue(h >= 0);
        assertTrue(h < 100);

        // Object with negative hashCode (e.g. some negative Integer)
        int h2 = HashUtil.hashCode(Integer.valueOf(-999), 53);
        assertTrue(h2 >= 0);
        assertTrue(h2 < 53);
    }

    @Test
    @DisplayName("getInitGroupSize always returns 3")
    void getInitGroupSize() {
        assertEquals(3, HashUtil.getInitGroupSize());
    }

    @Test
    @DisplayName("hashCode(int[], count) combines int hashes correctly")
    void hashCodeIntArrayWithCount() {
        HashUtil hu = new HashUtil(107);

        int[] hashes = {10, 20, 30, 40};
        int h = hu.hashCode(hashes, 4);
        assertTrue(h >= 0);
        assertTrue(h < hu.getCapacity());

        // count=1 should equal hashCode(int) on that single element
        int hOne = hu.hashCode(hashes, 1);
        int hDirect = hu.hashCode(10);
        assertEquals(hDirect, hOne);

        // Consistency: same input, same result
        assertEquals(h, hu.hashCode(hashes, 4));

        // With negative hashes
        int[] negHashes = {-5, -10};
        int hNeg = hu.hashCode(negHashes, 2);
        assertTrue(hNeg >= 0);
        assertTrue(hNeg < hu.getCapacity());
    }

    @Test
    @DisplayName("getPrevCapacity returns smaller prime or 11369 for small capacities")
    void getPrevCapacity() {
        // Capacity smaller than 11369 should return 11369
        HashUtil huSmall = new HashUtil(100);
        // 100 adjusts to prime 107, which is < 11369
        assertEquals(11369, huSmall.getPrevCapacity());

        // Capacity of exactly 11369 should return the prime before it (8737)
        HashUtil huExact = new HashUtil(11369);
        assertEquals(11369, huExact.getCapacity());
        assertEquals(8737, huExact.getPrevCapacity());

        // Larger capacity: 14783 is in the PRIMES table
        HashUtil huLarger = new HashUtil(14783);
        assertEquals(14783, huLarger.getCapacity());
        // getPrevCapacity should return 11369 (the prime before 14783)
        assertEquals(11369, huLarger.getPrevCapacity());

        // Capacity well above 11369
        HashUtil huBig = new HashUtil(50000);
        // 50000 adjusts to 65546729? No — let's check: 50000 is between 42257 and 54941
        // Actually looking at PRIMES: ..., 42257, 54941, ... so 50000 -> 54941
        assertEquals(54941, huBig.getCapacity());
        int prev = huBig.getPrevCapacity();
        assertEquals(42257, prev);
    }
}
