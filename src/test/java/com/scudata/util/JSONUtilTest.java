package com.scudata.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.BaseRecord;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;

/**
 * Tests for {@link JSONUtil}.
 * Covers parseJSON, toJSON, completeUnescape.
 */
class JSONUtilTest {

    // ========== parseJSON ==========

    @Nested
    @DisplayName("parseJSON")
    class ParseJSON {

        @Test
        @DisplayName("parse null/empty input returns null")
        void parseEmptyReturnsNull() {
            // whitespace-only string
            char[] chars = "   ".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertNull(result);
        }

        @Test
        @DisplayName("parse integer value")
        void parseInteger() {
            char[] chars = "42".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("parse negative integer")
        void parseNegativeInteger() {
            char[] chars = "-7".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertTrue(result instanceof Number);
            assertEquals(-7, ((Number) result).intValue());
        }

        @Test
        @DisplayName("parse floating point number")
        void parseDouble() {
            char[] chars = "3.14".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertTrue(result instanceof Number);
            assertEquals(3.14, ((Number) result).doubleValue(), 0.001);
        }

        @Test
        @DisplayName("parse true/false/null literals")
        void parseLiterals() {
            char[] t = "true".toCharArray();
            assertEquals(Boolean.TRUE, JSONUtil.parseJSON(t, 0, t.length - 1, null));

            char[] f = "false".toCharArray();
            assertEquals(Boolean.FALSE, JSONUtil.parseJSON(f, 0, f.length - 1, null));

            char[] n = "null".toCharArray();
            assertNull(JSONUtil.parseJSON(n, 0, n.length - 1, null));
        }

        @Test
        @DisplayName("parse quoted string")
        void parseString() {
            char[] chars = "\"hello\"".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertEquals("hello", result);
        }

        @Test
        @DisplayName("parse simple array [1,2,3]")
        void parseSimpleArray() {
            char[] chars = "[1,2,3]".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertNotNull(result);
            assertTrue(result instanceof Sequence);
            Sequence seq = (Sequence) result;
            assertEquals(3, seq.length());
            assertEquals(1, ((Number) seq.get(1)).intValue());
            assertEquals(2, ((Number) seq.get(2)).intValue());
            assertEquals(3, ((Number) seq.get(3)).intValue());
        }

        @Test
        @DisplayName("parse empty array []")
        void parseEmptyArray() {
            char[] chars = "[]".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            // empty array should parse to something; parseSequence on empty yields empty Sequence or null
            assertNotNull(result);
        }

        @Test
        @DisplayName("parse simple object {\"name\":\"John\",\"age\":30}")
        void parseSimpleObject() {
            String json = "{\"name\":\"John\",\"age\":30}";
            char[] chars = json.toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertNotNull(result);
            assertTrue(result instanceof BaseRecord);
            BaseRecord r = (BaseRecord) result;
            assertEquals("John", r.getFieldValue(r.getFieldIndex("name")));
            assertEquals(30, ((Number) r.getFieldValue(r.getFieldIndex("age"))).intValue());
        }

        @Test
        @DisplayName("parse nested object")
        void parseNestedObject() {
            String json = "{\"a\":{\"b\":1}}";
            char[] chars = json.toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertNotNull(result);
            assertTrue(result instanceof BaseRecord);
            BaseRecord outer = (BaseRecord) result;
            Object inner = outer.getFieldValue(outer.getFieldIndex("a"));
            assertTrue(inner instanceof BaseRecord);
            BaseRecord innerRecord = (BaseRecord) inner;
            assertEquals(1, ((Number) innerRecord.getFieldValue(innerRecord.getFieldIndex("b"))).intValue());
        }

        @Test
        @DisplayName("parse array of objects becomes Table")
        void parseArrayOfObjects() {
            String json = "[{\"x\":1},{\"x\":2}]";
            char[] chars = json.toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertNotNull(result);
            // Array of same-structure records should become a Table
            assertTrue(result instanceof Table);
            Table t = (Table) result;
            assertEquals(2, t.length());
        }

        @Test
        @DisplayName("parse with leading/trailing whitespace")
        void parseWithWhitespace() {
            char[] chars = "  42  ".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("parse mismatched brackets returns string")
        void parseMismatchedBrackets() {
            char[] chars = "[1,2".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
            // When [ does not match ], returns the string representation
            assertTrue(result instanceof String);
        }

        @Test
        @DisplayName("parse with start/end sub-range")
        void parseSubRange() {
            // "xxx42yyy" — parse positions 3..4 which is "42"
            char[] chars = "xxx42yyy".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 3, 4, null);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("two-arg parseJSON delegates to four-arg with null opt")
        void parseTwoArg() {
            char[] chars = "10".toCharArray();
            Object result = JSONUtil.parseJSON(chars, 0, chars.length - 1);
            assertTrue(result instanceof Number);
            assertEquals(10, ((Number) result).intValue());
        }
    }

    // ========== toJSON ==========

    @Nested
    @DisplayName("toJSON")
    class ToJSON {

        @Test
        @DisplayName("toJSON null")
        void toJSONNull() {
            StringBuffer sb = new StringBuffer();
            JSONUtil.toJSON(null, sb);
            assertEquals("null", sb.toString());
        }

        @Test
        @DisplayName("toJSON integer")
        void toJSONInteger() {
            StringBuffer sb = new StringBuffer();
            JSONUtil.toJSON(42, sb);
            assertEquals("42", sb.toString());
        }

        @Test
        @DisplayName("toJSON string is quoted and escaped")
        void toJSONString() {
            StringBuffer sb = new StringBuffer();
            JSONUtil.toJSON("hello", sb);
            String result = sb.toString();
            assertTrue(result.startsWith("\""));
            assertTrue(result.endsWith("\""));
            assertTrue(result.contains("hello"));
        }

        @Test
        @DisplayName("toJSON record")
        void toJSONRecord() {
            DataStruct ds = new DataStruct(new String[]{"name", "age"});
            Record r = new Record(ds, new Object[]{"Alice", 25});
            StringBuffer sb = new StringBuffer();
            JSONUtil.toJSON(r, sb);
            String result = sb.toString();
            assertTrue(result.startsWith("{"));
            assertTrue(result.endsWith("}"));
            assertTrue(result.contains("\"name\""));
            assertTrue(result.contains("\"Alice\""));
            assertTrue(result.contains("25"));
        }

        @Test
        @DisplayName("toJSON sequence")
        void toJSONSequence() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3});
            StringBuffer sb = new StringBuffer();
            JSONUtil.toJSON(seq, sb);
            assertEquals("[1,2,3]", sb.toString());
        }

        @Test
        @DisplayName("toJSON(Sequence) returns string")
        void toJSONSequenceString() {
            Sequence seq = new Sequence(new Object[]{10, 20});
            String result = JSONUtil.toJSON(seq);
            assertEquals("[10,20]", result);
        }

        @Test
        @DisplayName("toJSON(BaseRecord) returns string")
        void toJSONRecordString() {
            DataStruct ds = new DataStruct(new String[]{"k"});
            Record r = new Record(ds, new Object[]{99});
            String result = JSONUtil.toJSON(r);
            assertTrue(result.startsWith("{"));
            assertTrue(result.contains("99"));
            assertTrue(result.endsWith("}"));
        }
    }

    // ========== completeUnescape ==========

    @Nested
    @DisplayName("completeUnescape")
    class CompleteUnescape {

        @Test
        @DisplayName("no escape characters")
        void noEscape() {
            assertEquals("hello", JSONUtil.completeUnescape("hello"));
        }

        @Test
        @DisplayName("standard escape sequences")
        void standardEscapes() {
            assertEquals("\t", JSONUtil.completeUnescape("\\t"));
            assertEquals("\n", JSONUtil.completeUnescape("\\n"));
            assertEquals("\r", JSONUtil.completeUnescape("\\r"));
            assertEquals("\b", JSONUtil.completeUnescape("\\b"));
            assertEquals("\f", JSONUtil.completeUnescape("\\f"));
            assertEquals("\"", JSONUtil.completeUnescape("\\\""));
            assertEquals("\\", JSONUtil.completeUnescape("\\\\"));
        }

        @Test
        @DisplayName("unicode escape \\uXXXX")
        void unicodeEscape() {
            // \u0041 = 'A'
            assertEquals("A", JSONUtil.completeUnescape("\\u0041"));
        }

        @Test
        @DisplayName("unicode escape in context")
        void unicodeEscapeInContext() {
            assertEquals("hAllo", JSONUtil.completeUnescape("h\\u0041llo"));
        }

        @Test
        @DisplayName("trailing backslash is kept")
        void trailingBackslash() {
            String result = JSONUtil.completeUnescape("abc\\");
            assertEquals("abc\\", result);
        }

        @Test
        @DisplayName("unknown escape keeps the backslash")
        void unknownEscape() {
            // \x is not a known escape, should keep the backslash
            String result = JSONUtil.completeUnescape("\\x");
            assertEquals("\\x", result);
        }

        @Test
        @DisplayName("empty string")
        void emptyString() {
            assertEquals("", JSONUtil.completeUnescape(""));
        }
    }

    // ========== Round-trip ==========

    @Test
    @DisplayName("round-trip: toJSON then parseJSON for record")
    void roundTripRecord() {
        DataStruct ds = new DataStruct(new String[]{"id", "val"});
        Record r = new Record(ds, new Object[]{1, "test"});
        String json = JSONUtil.toJSON(r);
        char[] chars = json.toCharArray();
        Object parsed = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
        assertTrue(parsed instanceof BaseRecord);
        BaseRecord pr = (BaseRecord) parsed;
        assertEquals(1, ((Number) pr.getFieldValue(pr.getFieldIndex("id"))).intValue());
        assertEquals("test", pr.getFieldValue(pr.getFieldIndex("val")));
    }

    @Test
    @DisplayName("round-trip: toJSON then parseJSON for sequence")
    void roundTripSequence() {
        Sequence seq = new Sequence(new Object[]{10, 20, 30});
        String json = JSONUtil.toJSON(seq);
        char[] chars = json.toCharArray();
        Object parsed = JSONUtil.parseJSON(chars, 0, chars.length - 1, null);
        assertTrue(parsed instanceof Sequence);
        Sequence ps = (Sequence) parsed;
        assertEquals(3, ps.length());
        assertEquals(10, ((Number) ps.get(1)).intValue());
    }
}
