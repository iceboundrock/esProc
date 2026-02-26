package com.scudata.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SegmentSet - key=value pair collection parsed from delimited strings")
public class SegmentSetTest {

    @Test
    @DisplayName("Default constructor creates an empty set")
    void defaultConstructorCreatesEmptySet() {
        SegmentSet ss = new SegmentSet();
        assertTrue(ss.isEmpty());
        assertEquals(0, ss.size());
    }

    @Test
    @DisplayName("Parse simple string 'a=1;b=2'")
    void parseSimpleString() {
        SegmentSet ss = new SegmentSet("a=1;b=2");
        assertEquals(2, ss.size());
        assertEquals("1", ss.get("a"));
        assertEquals("2", ss.get("b"));
    }

    @Test
    @DisplayName("Key with no '=' sign results in empty value")
    void keyWithNoEqualsSignGivesEmptyValue() {
        SegmentSet ss = new SegmentSet("alpha;beta=hello");
        assertEquals(2, ss.size());
        assertEquals("", ss.get("alpha"));
        assertEquals("hello", ss.get("beta"));
    }

    @Test
    @DisplayName("Keys are case-insensitive by default (stored as lowercase)")
    void caseInsensitiveByDefault() {
        SegmentSet ss = new SegmentSet("ABC=100;XyZ=200");
        assertEquals("100", ss.get("abc"));
        assertEquals("100", ss.get("ABC"));
        assertEquals("200", ss.get("xyz"));
        assertEquals("200", ss.get("XYZ"));
        // keySet should contain lowercase keys
        assertTrue(ss.keySet().contains("abc"));
        assertTrue(ss.keySet().contains("xyz"));
        assertFalse(ss.keySet().contains("ABC"));
    }

    @Test
    @DisplayName("Case-sensitive mode preserves key casing")
    void caseSensitiveMode() {
        SegmentSet ss = new SegmentSet("ABC=100;abc=200", true);
        assertEquals(2, ss.size());
        assertEquals("100", ss.get("ABC"));
        assertEquals("200", ss.get("abc"));
        // mixed case should return null (not found)
        assertNull(ss.get("Abc"));
    }

    @Test
    @DisplayName("Custom delimiter parses correctly")
    void customDelimiter() {
        SegmentSet ss = new SegmentSet("x=10,y=20,z=30", ',');
        assertEquals(3, ss.size());
        assertEquals("10", ss.get("x"));
        assertEquals("20", ss.get("y"));
        assertEquals("30", ss.get("z"));
    }

    @Test
    @DisplayName("Constructor with separate keys and values strings")
    void separateKeysValuesConstructor() {
        SegmentSet ss = new SegmentSet("a;b;c", "1;2;3", ';');
        assertEquals(3, ss.size());
        assertEquals("1", ss.get("a"));
        assertEquals("2", ss.get("b"));
        assertEquals("3", ss.get("c"));
    }

    @Test
    @DisplayName("put and get basic operations")
    void putAndGet() {
        SegmentSet ss = new SegmentSet();
        ss.put("name", "Alice");
        ss.put("age", "30");
        assertEquals("Alice", ss.get("name"));
        assertEquals("30", ss.get("age"));
        assertEquals(2, ss.size());
    }

    @Test
    @DisplayName("put returns old value when key exists, null when new")
    void putReturnsOldValue() {
        SegmentSet ss = new SegmentSet();
        String first = ss.put("key", "val1");
        assertNull(first);
        String second = ss.put("key", "val2");
        assertEquals("val1", second);
        assertEquals("val2", ss.get("key"));
    }

    @Test
    @DisplayName("remove returns old value and reduces size")
    void removeReturnsOldValue() {
        SegmentSet ss = new SegmentSet("a=1;b=2;c=3");
        assertEquals("2", ss.remove("b"));
        assertEquals(2, ss.size());
        assertNull(ss.get("b"));
        // removing non-existent key returns null
        assertNull(ss.remove("nonexistent"));
    }

    @Test
    @DisplayName("containsKey respects case-insensitivity")
    void containsKeyCaseHandling() {
        SegmentSet ss = new SegmentSet("Hello=World");
        // default case-insensitive: stored as "hello"
        assertTrue(ss.containsKey("hello"));
        assertTrue(ss.containsKey("HELLO"));
        assertTrue(ss.containsKey("Hello"));

        SegmentSet sensitive = new SegmentSet("Hello=World", true);
        assertTrue(sensitive.containsKey("Hello"));
        assertFalse(sensitive.containsKey("hello"));
        assertFalse(sensitive.containsKey("HELLO"));
    }

    @Test
    @DisplayName("containsValue checks values correctly")
    void containsValueTest() {
        SegmentSet ss = new SegmentSet("a=Apple;b=Banana");
        assertTrue(ss.containsValue("Apple"));
        assertTrue(ss.containsValue("Banana"));
        assertFalse(ss.containsValue("Cherry"));
        // values are not lowercased
        assertFalse(ss.containsValue("apple"));
    }

    @Test
    @DisplayName("size, isEmpty, and clear work correctly")
    void sizeIsEmptyClear() {
        SegmentSet ss = new SegmentSet("a=1;b=2;c=3");
        assertEquals(3, ss.size());
        assertFalse(ss.isEmpty());

        ss.clear();
        assertEquals(0, ss.size());
        assertTrue(ss.isEmpty());
    }

    @Test
    @DisplayName("Null and blank keys/values become empty string")
    void nullAndBlankBecomeEmptyString() {
        SegmentSet ss = new SegmentSet();
        // null key becomes ""
        ss.put(null, "val");
        assertEquals("val", ss.get(""));
        assertEquals("val", ss.get(null));

        // null value becomes ""
        ss.put("k", null);
        assertEquals("", ss.get("k"));

        // parsing segment "=343" gives empty key with value 343
        SegmentSet ss2 = new SegmentSet("=343");
        assertTrue(ss2.containsKey(""));
        assertEquals("343", ss2.get(""));
    }

    @Test
    @DisplayName("putAll from Map adds all entries")
    void putAllFromMap() {
        SegmentSet ss = new SegmentSet();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("x", "10");
        map.put("y", "20");
        ss.putAll(map);
        assertEquals(2, ss.size());
        assertEquals("10", ss.get("x"));
        assertEquals("20", ss.get("y"));
    }

    @Test
    @DisplayName("putAll from keys/values strings")
    void putAllFromKeysValuesStrings() {
        SegmentSet ss = new SegmentSet("existing=0");
        ss.putAll("a;b;c", "1;2;3", ';');
        assertEquals(4, ss.size());
        assertEquals("0", ss.get("existing"));
        assertEquals("1", ss.get("a"));
        assertEquals("2", ss.get("b"));
        assertEquals("3", ss.get("c"));
    }

    @Test
    @DisplayName("putAll from another SegmentSet")
    void putAllFromSegmentSet() {
        SegmentSet ss1 = new SegmentSet("a=1;b=2");
        SegmentSet ss2 = new SegmentSet("c=3;d=4");
        ss1.putAll(ss2);
        assertEquals(4, ss1.size());
        assertEquals("1", ss1.get("a"));
        assertEquals("3", ss1.get("c"));
        assertEquals("4", ss1.get("d"));
    }

    @Test
    @DisplayName("containsKeys with delimited string")
    void containsKeysWithDelimitedString() {
        SegmentSet ss = new SegmentSet("a=1;b=2;c=3");
        assertTrue(ss.containsKeys("a;b;c", ';'));
        assertTrue(ss.containsKeys("a;c", ';'));
        assertFalse(ss.containsKeys("a;d", ';'));
    }

    @Test
    @DisplayName("containsKeys with Set")
    void containsKeysWithSet() {
        SegmentSet ss = new SegmentSet("a=1;b=2;c=3");
        Set<String> present = new HashSet<>(Arrays.asList("a", "b"));
        assertTrue(ss.containsKeys(present));

        Set<String> missing = new HashSet<>(Arrays.asList("a", "z"));
        assertFalse(ss.containsKeys(missing));

        // null or empty set returns true
        assertTrue(ss.containsKeys((Set) null));
        assertTrue(ss.containsKeys(new HashSet<>()));
    }

    @Test
    @DisplayName("getValues returns delimited values for given keys")
    void getValuesTest() {
        SegmentSet ss = new SegmentSet("a=1;b=2;c=3");
        String result = ss.getValues("a;c", "N/A", ';');
        assertEquals("1;3", result);

        // missing key gets valueIfBlank
        ss.put("d", "");
        String result2 = ss.getValues("a;d;c", "DEFAULT", ';');
        assertEquals("1;DEFAULT;3", result2);

        // null keys returns null
        assertNull(ss.getValues(null, "x", ';'));
        // null valueIfBlank returns null
        assertNull(ss.getValues("a", null, ';'));
    }

    @Test
    @DisplayName("toString with default ';' delimiter")
    void toStringDefault() {
        SegmentSet ss = new SegmentSet("a=1;b=2");
        String str = ss.toString();
        // LinkedHashMap preserves insertion order
        assertEquals("a=1;b=2", str);
    }

    @Test
    @DisplayName("toString with custom delimiter")
    void toStringCustomDelimiter() {
        SegmentSet ss = new SegmentSet("a=1;b=2");
        String str = ss.toString(" AND ");
        assertEquals("a=1 AND b=2", str);
    }

    @Test
    @DisplayName("toString returns null for empty set")
    void toStringEmptyReturnsNull() {
        SegmentSet ss = new SegmentSet();
        assertNull(ss.toString());
        assertNull(ss.toString(","));
    }

    @Test
    @DisplayName("toMap returns an independent copy of internal map")
    void toMapReturnsIndependentCopy() {
        SegmentSet ss = new SegmentSet("a=1;b=2");
        Map map = ss.toMap();
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));

        // modifying the returned map does not affect SegmentSet
        map.put("a", "changed");
        assertEquals("1", ss.get("a"));

        // modifying SegmentSet does not affect previously returned map
        ss.put("c", "3");
        assertFalse(map.containsKey("c"));
    }

    @Test
    @DisplayName("trimBlank=false preserves surrounding whitespace")
    void trimBlankFalsePreservesWhitespace() {
        SegmentSet ss = new SegmentSet(" a = 1 ; b = 2 ", false, ';', false);
        // keys and values should retain their whitespace
        assertTrue(ss.containsKey(" a "));
        assertEquals(" 1 ", ss.get(" a "));
        assertTrue(ss.containsKey(" b "));
        assertEquals(" 2 ", ss.get(" b "));

        // with trim (default), whitespace is stripped
        SegmentSet trimmed = new SegmentSet(" a = 1 ; b = 2 ", false, ';', true);
        assertTrue(trimmed.containsKey("a"));
        assertEquals("1", trimmed.get("a"));
    }

    @Test
    @DisplayName("Whitespace around keys and values is trimmed by default")
    void defaultTrimsWhitespace() {
        SegmentSet ss = new SegmentSet("  key1  =  val1  ;  key2  =  val2  ");
        assertEquals("val1", ss.get("key1"));
        assertEquals("val2", ss.get("key2"));
        assertEquals(2, ss.size());
    }
}
