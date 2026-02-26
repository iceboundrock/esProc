package com.scudata.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectCache - Integer/String caching")
public class ObjectCacheTest {

    @Test
    void cachedIntegerIdentity() {
        Integer a = ObjectCache.getInteger(42);
        Integer b = ObjectCache.getInteger(42);
        assertSame(a, b, "Cached integers should be the same instance");
    }

    @Test
    void cachedIntegerZero() {
        Integer zero = ObjectCache.getInteger(0);
        assertEquals(0, zero.intValue());
        assertSame(zero, ObjectCache.getInteger(0));
    }

    @Test
    void cachedIntegerMax() {
        Integer max = ObjectCache.getInteger(65535);
        assertEquals(65535, max.intValue());
        assertSame(max, ObjectCache.getInteger(65535));
    }

    @Test
    void uncachedIntegerAboveMax() {
        Integer a = ObjectCache.getInteger(65536);
        Integer b = ObjectCache.getInteger(65536);
        assertEquals(a, b);
        // these may or may not be the same instance, but values are equal
        assertEquals(65536, a.intValue());
    }

    @Test
    void uncachedNegativeInteger() {
        Integer neg = ObjectCache.getInteger(-1);
        assertEquals(-1, neg.intValue());
    }

    @Test
    void cachedStringAscii() {
        String a = ObjectCache.getString((byte) 65); // 'A'
        String b = ObjectCache.getString((byte) 65);
        assertSame(a, b, "ASCII strings should be cached");
        assertEquals("A", a);
    }

    @Test
    void cachedStringFromCharArray() {
        char[] buf = {'A'};
        String a = ObjectCache.getString(buf);
        String b = ObjectCache.getString(buf);
        assertSame(a, b, "ASCII char arrays should use cached strings");
        assertEquals("A", a);
    }

    @Test
    void uncachedStringHighChar() {
        char[] buf = {'\u00FF'}; // > 127
        String s = ObjectCache.getString(buf);
        assertEquals("\u00FF", s);
    }
}
