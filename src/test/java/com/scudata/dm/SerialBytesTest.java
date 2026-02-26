package com.scudata.dm;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.scudata.common.RQException;

class SerialBytesTest {

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    @Test
    @DisplayName("No-arg constructor initialises both values to zero")
    void noArgConstructor() {
        SerialBytes sb = new SerialBytes();
        assertEquals(0L, sb.getValue1());
        assertEquals(0L, sb.getValue2());
    }

    @Test
    @DisplayName("Two-long constructor stores value1 and value2 directly")
    void twoLongConstructor() {
        SerialBytes sb = new SerialBytes(0x0102030405060708L, 0x090A0B0C0D0E0F10L);
        assertEquals(0x0102030405060708L, sb.getValue1());
        assertEquals(0x090A0B0C0D0E0F10L, sb.getValue2());
    }

    @Test
    @DisplayName("Byte-array constructor distributes bytes into value1 and value2")
    void byteArrayConstructor() {
        byte[] bytes = new byte[] {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
        };
        SerialBytes sb = new SerialBytes(bytes, 16);
        assertEquals(0x0102030405060708L, sb.getValue1());
        assertEquals(0x090A0B0C0D0E0F10L, sb.getValue2());
    }

    @Test
    @DisplayName("Byte-array constructor with fewer than 8 bytes only sets value1")
    void byteArrayConstructorShort() {
        byte[] bytes = new byte[] { 0x01, 0x02, 0x03 };
        SerialBytes sb = new SerialBytes(bytes, 3);
        assertEquals(0x0102030000000000L, sb.getValue1());
        assertEquals(0L, sb.getValue2());
    }

    @Test
    @DisplayName("Number constructor with len <= 8 stores value in value1 shifted left")
    void numberConstructorSmallLen() {
        // Number 0x0102 with len=2 should be shifted to highest 2 bytes of value1
        SerialBytes sb = new SerialBytes(0x0102L, 2);
        assertEquals(0x0102000000000000L, sb.getValue1());
        assertEquals(0L, sb.getValue2());
    }

    @Test
    @DisplayName("Number constructor with len > 8 distributes across value1 and value2")
    void numberConstructorLargeLen() {
        // Use a value that fits in 9 bytes
        SerialBytes sb = new SerialBytes(1L, 9);
        // 1 as a BigInteger is [0x01], 9-byte len means 8 zero bytes then 0x01
        // index goes: len - blen = 9 - 1 = 8 zero bytes prefix, then byte 0x01 at index 9
        // index 9 goes to value2: (0xFF & 0x01) << (16-9)*8 = 0x01 << 56
        assertEquals(0L, sb.getValue1());
        assertEquals(0x0100000000000000L, sb.getValue2());
    }

    @Test
    @DisplayName("Number constructor throws RQException when len > 16")
    void numberConstructorLenExceedsLimit() {
        assertThrows(RQException.class, () -> new SerialBytes(42, 17));
    }

    @Test
    @DisplayName("Multi-value constructor packs multiple numbers into the 16 bytes")
    void multiValueConstructor() {
        // Two 4-byte numbers packed into value1
        Number[] vals = { 0x01020304L, 0x05060708L };
        int[] lens = { 4, 4 };
        SerialBytes sb = new SerialBytes(vals, lens);
        assertEquals(0x0102030405060708L, sb.getValue1());
        assertEquals(0L, sb.getValue2());
    }

    @Test
    @DisplayName("Multi-value constructor throws RQException when total bytes exceed 16")
    void multiValueConstructorExceedsLimit() {
        Number[] vals = { 1L, 2L };
        int[] lens = { 10, 8 };
        assertThrows(RQException.class, () -> new SerialBytes(vals, lens));
    }

    // ---------------------------------------------------------------
    // length
    // ---------------------------------------------------------------

    @Test
    @DisplayName("length always returns 16")
    void lengthAlways16() {
        assertEquals(16, new SerialBytes().length());
        assertEquals(16, new SerialBytes(123L, 456L).length());
    }

    // ---------------------------------------------------------------
    // hashCode
    // ---------------------------------------------------------------

    @Test
    @DisplayName("hashCode uses HashUtil.hashCode(value1 + value2)")
    void hashCodeTest() {
        long v1 = 0x0102030405060708L;
        long v2 = 0x090A0B0C0D0E0F10L;
        SerialBytes sb = new SerialBytes(v1, v2);
        long sum = v1 + v2;
        int expected = (int)(sum ^ (sum >>> 32));
        assertEquals(expected, sb.hashCode());

        // Zero case
        SerialBytes zero = new SerialBytes();
        assertEquals(0, zero.hashCode());
    }

    // ---------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toString returns zero-padded 32-char hex string")
    void toStringZeroPadded() {
        // All zeros → 32 zeroes
        SerialBytes zero = new SerialBytes();
        assertEquals("00000000000000000000000000000000", zero.toString());
        assertEquals(32, zero.toString().length());

        // Known values
        SerialBytes sb = new SerialBytes(0x0000000000000001L, 0x0000000000000002L);
        String hex = sb.toString();
        assertEquals(32, hex.length());
        assertEquals("00000000000000010000000000000002", hex);

        // Large values (negative longs produce hex without sign)
        SerialBytes big = new SerialBytes(-1L, -1L);
        assertEquals("ffffffffffffffffffffffffffffffff", big.toString());
    }

    // ---------------------------------------------------------------
    // toByteArray
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toByteArray returns 16-byte array matching constructor input")
    void toByteArrayRoundTrip() {
        byte[] original = {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
        };
        SerialBytes sb = new SerialBytes(original, 16);
        byte[] result = sb.toByteArray();
        assertEquals(16, result.length);
        assertArrayEquals(original, result);
    }

    // ---------------------------------------------------------------
    // getByte
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getByte returns correct byte at 1-based positions")
    void getByteValidPositions() {
        byte[] bytes = {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
        };
        SerialBytes sb = new SerialBytes(bytes, 16);

        // value1 bytes (positions 1-8)
        assertEquals(0x01L, sb.getByte(1));
        assertEquals(0x04L, sb.getByte(4));
        assertEquals(0x08L, sb.getByte(8));

        // value2 bytes (positions 9-16)
        assertEquals(0x09L, sb.getByte(9));
        assertEquals(0x0CL, sb.getByte(12));
        assertEquals(0x10L, sb.getByte(16));
    }

    @Test
    @DisplayName("getByte throws RQException for out-of-bounds positions")
    void getByteInvalidPosition() {
        SerialBytes sb = new SerialBytes();
        assertThrows(RQException.class, () -> sb.getByte(0));
        assertThrows(RQException.class, () -> sb.getByte(17));
        assertThrows(RQException.class, () -> sb.getByte(-1));
    }

    // ---------------------------------------------------------------
    // getBytes
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getBytes returns correct value for ranges within value1, spanning both, and within value2")
    void getBytesValidRanges() {
        SerialBytes sb = new SerialBytes(0x0102030405060708L, 0x090A0B0C0D0E0F10L);

        // Entirely within value1
        assertEquals(0x01L, sb.getBytes(1, 1));
        assertEquals(0x0102L, sb.getBytes(1, 2));
        assertEquals(0x0102030405060708L, sb.getBytes(1, 8));

        // Spanning value1 and value2
        assertEquals(0x0708090AL, sb.getBytes(7, 10));

        // Entirely within value2
        assertEquals(0x09L, sb.getBytes(9, 9));
        assertEquals(0x0F10L, sb.getBytes(15, 16));
    }

    @Test
    @DisplayName("getBytes throws RQException for invalid ranges")
    void getBytesInvalidRanges() {
        SerialBytes sb = new SerialBytes();
        assertThrows(RQException.class, () -> sb.getBytes(0, 1));   // start < 1
        assertThrows(RQException.class, () -> sb.getBytes(5, 3));   // end < start
        assertThrows(RQException.class, () -> sb.getBytes(1, 17));  // end > 16
    }

    // ---------------------------------------------------------------
    // compareTo & static compare
    // ---------------------------------------------------------------

    @Test
    @DisplayName("compareTo returns 0 for equal, negative for less, positive for greater, and handles unsigned semantics")
    void compareToVariousCases() {
        SerialBytes a = new SerialBytes(100L, 200L);
        SerialBytes b = new SerialBytes(100L, 200L);
        SerialBytes c = new SerialBytes(100L, 300L);
        SerialBytes d = new SerialBytes(200L, 100L);

        // Equal
        assertEquals(0, a.compareTo(b));

        // Same value1, different value2
        assertTrue(a.compareTo(c) < 0);
        assertTrue(c.compareTo(a) > 0);

        // Different value1
        assertTrue(a.compareTo(d) < 0);
        assertTrue(d.compareTo(a) > 0);

        // Unsigned semantics: negative long treated as larger than positive long
        // -1L is 0xFFFFFFFFFFFFFFFF, which is the maximum unsigned value
        SerialBytes negV1 = new SerialBytes(-1L, 0L);
        SerialBytes posV1 = new SerialBytes(1L, 0L);
        assertTrue(negV1.compareTo(posV1) > 0, "Negative value1 should compare as greater (unsigned)");
        assertTrue(posV1.compareTo(negV1) < 0);

        // Unsigned semantics for value2 when value1 is equal
        SerialBytes negV2 = new SerialBytes(0L, -1L);
        SerialBytes posV2 = new SerialBytes(0L, 1L);
        assertTrue(negV2.compareTo(posV2) > 0, "Negative value2 should compare as greater (unsigned)");
        assertTrue(posV2.compareTo(negV2) < 0);

        // Both negative: compare within negative range
        SerialBytes negA = new SerialBytes(-10L, 0L);
        SerialBytes negB = new SerialBytes(-5L, 0L);
        // -5L > -10L in signed, and in unsigned: 0xFFFF...F6 > 0xFFFF...FB? No.
        // -5L is 0xFFFFFFFFFFFFFFB, -10L is 0xFFFFFFFFFFFFFFF6
        // -5 > -10 in signed, so compareTo should return 1 for negB vs negA
        assertTrue(negB.compareTo(negA) > 0);
        assertTrue(negA.compareTo(negB) < 0);
    }

    @Test
    @DisplayName("Static compare method returns 1, 0, or -1")
    void staticCompareMethod() {
        assertEquals(0, SerialBytes.compare(5L, 10L, 5L, 10L));
        assertEquals(1, SerialBytes.compare(10L, 0L, 5L, 0L));
        assertEquals(-1, SerialBytes.compare(5L, 0L, 10L, 0L));

        // Unsigned: negative value1 > positive value1
        assertEquals(1, SerialBytes.compare(-1L, 0L, 1L, 0L));
        assertEquals(-1, SerialBytes.compare(1L, 0L, -1L, 0L));
    }

    // ---------------------------------------------------------------
    // equals
    // ---------------------------------------------------------------

    @Test
    @DisplayName("equals checks SerialBytes equality and rejects non-SerialBytes objects")
    void equalsTest() {
        SerialBytes a = new SerialBytes(10L, 20L);
        SerialBytes b = new SerialBytes(10L, 20L);
        SerialBytes c = new SerialBytes(10L, 21L);
        SerialBytes d = new SerialBytes(11L, 20L);

        // Same values
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));

        // Different value2
        assertFalse(a.equals(c));

        // Different value1
        assertFalse(a.equals(d));

        // Non-SerialBytes object
        assertFalse(a.equals("not a SerialBytes"));
        assertFalse(a.equals(null));
        assertFalse(a.equals(10L));
    }

    // ---------------------------------------------------------------
    // getValue1 / getValue2
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getValue1 and getValue2 return stored values including negative longs")
    void getValueAccessors() {
        SerialBytes sb = new SerialBytes(Long.MIN_VALUE, Long.MAX_VALUE);
        assertEquals(Long.MIN_VALUE, sb.getValue1());
        assertEquals(Long.MAX_VALUE, sb.getValue2());
    }
}
