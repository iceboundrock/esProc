package com.scudata.dm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.common.RQException;

/**
 * Tests for {@link Record} — field-level data container with 0-based field indexing.
 */
@DisplayName("Record")
class RecordTest {

    private DataStruct ds;

    @BeforeEach
    void setUp() {
        ds = new DataStruct(new String[]{"name", "age", "city"});
    }

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("Record(DataStruct) creates record with null field values")
        void constructWithDs() {
            Record r = new Record(ds);
            assertEquals(3, r.getFieldCount());
            assertNull(r.getFieldValue(0));
            assertNull(r.getFieldValue(1));
            assertNull(r.getFieldValue(2));
        }

        @Test
        @DisplayName("Record(DataStruct, Object[]) copies initial values")
        void constructWithInitVals() {
            Record r = new Record(ds, new Object[]{"Alice", 30, "NYC"});
            assertEquals("Alice", r.getFieldValue(0));
            assertEquals(30, r.getFieldValue(1));
            assertEquals("NYC", r.getFieldValue(2));
        }

        @Test
        @DisplayName("No-arg constructor for serialization")
        void noArgConstructor() {
            Record r = new Record();
            assertNotNull(r);
        }
    }

    // ---------------------------------------------------------------
    // dataStruct / setDataStruct
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("DataStruct accessors")
    class DataStructTests {

        @Test
        @DisplayName("dataStruct() returns the struct")
        void getDataStruct() {
            Record r = new Record(ds);
            assertSame(ds, r.dataStruct());
        }

        @Test
        @DisplayName("setDataStruct changes the struct")
        void setDataStruct() {
            Record r = new Record(ds);
            DataStruct ds2 = new DataStruct(new String[]{"x", "y"});
            r.setDataStruct(ds2);
            assertSame(ds2, r.dataStruct());
        }
    }

    // ---------------------------------------------------------------
    // getFieldValue / set — by index
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Field access by index")
    class FieldByIndexTests {

        @Test
        @DisplayName("getFieldValue(int) and set(int, Object)")
        void getAndSet() {
            Record r = new Record(ds);
            r.set(0, "Bob");
            r.set(1, 25);
            r.set(2, "London");

            assertEquals("Bob", r.getFieldValue(0));
            assertEquals(25, r.getFieldValue(1));
            assertEquals("London", r.getFieldValue(2));
        }

        @Test
        @DisplayName("Negative index wraps from end")
        void negativeIndex() {
            Record r = new Record(ds, new Object[]{"A", "B", "C"});
            // -1 => values.length - 1 = 2 => "C"
            assertEquals("C", r.getFieldValue(-1));
            assertEquals("B", r.getFieldValue(-2));
            assertEquals("A", r.getFieldValue(-3));
        }

        @Test
        @DisplayName("set with negative index wraps from end")
        void setNegativeIndex() {
            Record r = new Record(ds, new Object[]{"A", "B", "C"});
            r.set(-1, "Z");
            assertEquals("Z", r.getFieldValue(2));
        }

        @Test
        @DisplayName("getFieldValue throws for out-of-bounds positive index")
        void outOfBoundsPositive() {
            Record r = new Record(ds);
            assertThrows(RQException.class, () -> r.getFieldValue(3));
        }

        @Test
        @DisplayName("getFieldValue throws for out-of-bounds negative index")
        void outOfBoundsNegative() {
            Record r = new Record(ds);
            assertThrows(RQException.class, () -> r.getFieldValue(-4));
        }

        @Test
        @DisplayName("set throws for out-of-bounds index")
        void setOutOfBounds() {
            Record r = new Record(ds);
            assertThrows(RQException.class, () -> r.set(3, "x"));
            assertThrows(RQException.class, () -> r.set(-4, "x"));
        }
    }

    // ---------------------------------------------------------------
    // getFieldValue / set — by name
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Field access by name")
    class FieldByNameTests {

        @Test
        @DisplayName("getFieldValue(String) retrieves by field name")
        void getByName() {
            Record r = new Record(ds, new Object[]{"Alice", 30, "NYC"});
            assertEquals("Alice", r.getFieldValue("name"));
            assertEquals(30, r.getFieldValue("age"));
            assertEquals("NYC", r.getFieldValue("city"));
        }

        @Test
        @DisplayName("set(String, Object) sets by field name")
        void setByName() {
            Record r = new Record(ds);
            r.set("name", "Charlie");
            assertEquals("Charlie", r.getFieldValue("name"));
        }

        @Test
        @DisplayName("getFieldValue throws for unknown field name")
        void unknownFieldName() {
            Record r = new Record(ds);
            assertThrows(RQException.class, () -> r.getFieldValue("unknown"));
        }

        @Test
        @DisplayName("set(String) throws for unknown field name")
        void setUnknownFieldName() {
            Record r = new Record(ds);
            assertThrows(RQException.class, () -> r.set("unknown", "x"));
        }
    }

    // ---------------------------------------------------------------
    // getNormalFieldValue / setNormalFieldValue (no bounds check)
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Normal field value (no bounds check)")
    class NormalFieldTests {

        @Test
        @DisplayName("getNormalFieldValue returns value without bounds check")
        void getNormal() {
            Record r = new Record(ds, new Object[]{"A", "B", "C"});
            assertEquals("A", r.getNormalFieldValue(0));
            assertEquals("C", r.getNormalFieldValue(2));
        }

        @Test
        @DisplayName("setNormalFieldValue sets value without bounds check")
        void setNormal() {
            Record r = new Record(ds);
            r.setNormalFieldValue(1, 99);
            assertEquals(99, r.getNormalFieldValue(1));
        }
    }

    // ---------------------------------------------------------------
    // getFieldValue2 — returns null for out-of-bounds
    // ---------------------------------------------------------------
    @Test
    @DisplayName("getFieldValue2 returns null instead of throwing on invalid index")
    void getFieldValue2() {
        Record r = new Record(ds, new Object[]{"A", "B", "C"});
        assertEquals("C", r.getFieldValue2(2));
        assertNull(r.getFieldValue2(5));   // positive out of bounds
        assertNull(r.getFieldValue2(-4));  // negative out of bounds
        assertEquals("A", r.getFieldValue2(-3)); // valid negative
    }

    // ---------------------------------------------------------------
    // set2 — silent on invalid index
    // ---------------------------------------------------------------
    @Test
    @DisplayName("set2 silently ignores out-of-bounds index")
    void set2() {
        Record r = new Record(ds, new Object[]{"A", "B", "C"});
        r.set2(5, "X");      // positive out of bounds — no-op
        r.set2(-4, "X");     // negative out of bounds — no-op
        assertEquals("A", r.getFieldValue(0)); // unchanged
        assertEquals("C", r.getFieldValue(2)); // unchanged

        r.set2(0, "Z");
        assertEquals("Z", r.getFieldValue(0));

        r.set2(-1, "W");
        assertEquals("W", r.getFieldValue(2));
    }

    // ---------------------------------------------------------------
    // getFieldCount / getFieldNames / getFieldValues / getFieldIndex
    // ---------------------------------------------------------------
    @Test
    @DisplayName("getFieldCount returns number of fields")
    void getFieldCount() {
        Record r = new Record(ds);
        assertEquals(3, r.getFieldCount());
    }

    @Test
    @DisplayName("getFieldNames returns struct field names")
    void getFieldNames() {
        Record r = new Record(ds);
        assertArrayEquals(new String[]{"name", "age", "city"}, r.getFieldNames());
    }

    @Test
    @DisplayName("getFieldValues returns values array")
    void getFieldValues() {
        Record r = new Record(ds, new Object[]{"A", "B", "C"});
        Object[] vals = r.getFieldValues();
        assertEquals(3, vals.length);
        assertEquals("A", vals[0]);
    }

    @Test
    @DisplayName("getFieldIndex delegates to DataStruct")
    void getFieldIndex() {
        Record r = new Record(ds);
        assertEquals(0, r.getFieldIndex("name"));
        assertEquals(1, r.getFieldIndex("age"));
        assertEquals(-1, r.getFieldIndex("unknown"));
    }

    // ---------------------------------------------------------------
    // compare / isEquals
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Record comparison")
    class ComparisonTests {

        @Test
        @DisplayName("compare returns 0 for same record")
        void compareSameInstance() {
            Record r = new Record(ds, new Object[]{1, 2, 3});
            assertEquals(0, r.compare(r));
        }

        @Test
        @DisplayName("compare returns 1 when compared to null")
        void compareToNull() {
            Record r = new Record(ds, new Object[]{1, 2, 3});
            assertEquals(1, r.compare(null));
        }

        @Test
        @DisplayName("compare compares field-by-field")
        void compareFieldByField() {
            Record r1 = new Record(ds, new Object[]{1, 2, 3});
            Record r2 = new Record(ds, new Object[]{1, 2, 4});
            assertTrue(r1.compare(r2) < 0);
            assertTrue(r2.compare(r1) > 0);
        }

        @Test
        @DisplayName("compare handles different field counts")
        void compareDifferentFieldCount() {
            DataStruct ds2 = new DataStruct(new String[]{"name", "age"});
            Record r1 = new Record(ds, new Object[]{1, 2, 3});
            Record r2 = new Record(ds2, new Object[]{1, 2});
            // Same first 2 fields, r1 has more → r1 > r2
            assertTrue(r1.compare(r2) > 0);
            assertTrue(r2.compare(r1) < 0);
        }

        @Test
        @DisplayName("isEquals returns true for same values")
        void isEqualsSameValues() {
            Record r1 = new Record(ds, new Object[]{"A", 10, "X"});
            Record r2 = new Record(ds, new Object[]{"A", 10, "X"});
            assertTrue(r1.isEquals(r2));
        }

        @Test
        @DisplayName("isEquals returns false for different values")
        void isEqualsDifferentValues() {
            Record r1 = new Record(ds, new Object[]{"A", 10, "X"});
            Record r2 = new Record(ds, new Object[]{"A", 20, "X"});
            assertFalse(r1.isEquals(r2));
        }

        @Test
        @DisplayName("isEquals returns false for null argument")
        void isEqualsNull() {
            Record r = new Record(ds, new Object[]{1, 2, 3});
            assertFalse(r.isEquals(null));
        }

        @Test
        @DisplayName("isEquals returns true for same instance")
        void isEqualsSameInstance() {
            Record r = new Record(ds, new Object[]{1, 2, 3});
            assertTrue(r.isEquals(r));
        }

        @Test
        @DisplayName("isEquals returns false for different field count")
        void isEqualsDifferentCount() {
            DataStruct ds2 = new DataStruct(new String[]{"a", "b"});
            Record r1 = new Record(ds, new Object[]{1, 2, 3});
            Record r2 = new Record(ds2, new Object[]{1, 2});
            assertFalse(r1.isEquals(r2));
        }
    }

    // ---------------------------------------------------------------
    // key / value
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("key() and value()")
    class KeyValueTests {

        @Test
        @DisplayName("key() returns null when no primary key is defined")
        void keyNoPk() {
            Record r = new Record(ds, new Object[]{"A", 1, "X"});
            assertNull(r.key());
        }

        @Test
        @DisplayName("key() returns single PK field value")
        void keySinglePk() {
            ds.setPrimary(new String[]{"name"});
            Record r = new Record(ds, new Object[]{"Alice", 30, "NYC"});
            assertEquals("Alice", r.key());
        }

        @Test
        @DisplayName("key() returns sequence for composite PK")
        void keyCompositePk() {
            ds.setPrimary(new String[]{"name", "city"});
            Record r = new Record(ds, new Object[]{"Alice", 30, "NYC"});
            Object key = r.key();
            assertInstanceOf(Sequence.class, key);
            Sequence keySeq = (Sequence) key;
            assertEquals(2, keySeq.length());
            assertEquals("Alice", keySeq.get(1));
            assertEquals("NYC", keySeq.get(2));
        }

        @Test
        @DisplayName("value() returns sequence of all fields when no PK")
        void valueNoPk() {
            Record r = new Record(ds, new Object[]{"A", 10, "X"});
            Object v = r.value();
            assertInstanceOf(Sequence.class, v);
            Sequence seq = (Sequence) v;
            assertEquals(3, seq.length());
            assertEquals("A", seq.get(1));
            assertEquals(10, seq.get(2));
            assertEquals("X", seq.get(3));
        }

        @Test
        @DisplayName("value() returns PK value(s) when PK is set")
        void valueWithPk() {
            ds.setPrimary(new String[]{"name"});
            Record r = new Record(ds, new Object[]{"Alice", 30, "NYC"});
            assertEquals("Alice", r.value());
        }
    }

    // ---------------------------------------------------------------
    // paste
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("paste()")
    class PasteTests {

        @Test
        @DisplayName("paste(BaseRecord, false) copies by position")
        void pasteByPosition() {
            Record r1 = new Record(ds);
            Record src = new Record(ds, new Object[]{"A", 1, "B"});
            r1.paste(src, false);
            assertEquals("A", r1.getFieldValue(0));
            assertEquals(1, r1.getFieldValue(1));
            assertEquals("B", r1.getFieldValue(2));
        }

        @Test
        @DisplayName("paste(BaseRecord, true) copies by field name")
        void pasteByName() {
            DataStruct ds2 = new DataStruct(new String[]{"city", "name"});
            Record src = new Record(ds2, new Object[]{"London", "Eve"});
            Record r = new Record(ds);
            r.paste(src, true);
            assertEquals("Eve", r.getFieldValue(0));    // name
            assertEquals("London", r.getFieldValue(2));  // city
            assertNull(r.getFieldValue(1));               // age not in src
        }

        @Test
        @DisplayName("paste(null) does nothing")
        void pasteNull() {
            Record r = new Record(ds, new Object[]{"A", "B", "C"});
            r.paste((BaseRecord) null, false);
            assertEquals("A", r.getFieldValue(0));
        }

        @Test
        @DisplayName("paste(Sequence) copies elements as field values")
        void pasteSequence() {
            Sequence seq = new Sequence(new Object[]{"X", "Y", "Z"});
            Record r = new Record(ds);
            r.paste(seq);
            assertEquals("X", r.getFieldValue(0));
            assertEquals("Y", r.getFieldValue(1));
            assertEquals("Z", r.getFieldValue(2));
        }

        @Test
        @DisplayName("paste(null Sequence) does nothing")
        void pasteNullSequence() {
            Record r = new Record(ds, new Object[]{"A", "B", "C"});
            r.paste((Sequence) null);
            assertEquals("A", r.getFieldValue(0));
        }
    }

    // ---------------------------------------------------------------
    // derive
    // ---------------------------------------------------------------
    @Test
    @DisplayName("derive() extends record with new columns")
    void derive() {
        Record r = new Record(ds, new Object[]{"A", 1, "B"});
        DataStruct newDs = new DataStruct(new String[]{"name", "age", "city", "score"});
        r.derive(newDs);
        assertEquals(4, r.getFieldCount());
        assertEquals("A", r.getNormalFieldValue(0));
        assertNull(r.getNormalFieldValue(3)); // new column
        assertSame(newDs, r.dataStruct());
    }

    // ---------------------------------------------------------------
    // set(BaseRecord) — copy all fields
    // ---------------------------------------------------------------
    @Test
    @DisplayName("set(BaseRecord) copies all field values from source")
    void setFromRecord() {
        Record r1 = new Record(ds);
        Record r2 = new Record(ds, new Object[]{"X", 99, "Y"});
        r1.set(r2);
        assertEquals("X", r1.getFieldValue(0));
        assertEquals(99, r1.getFieldValue(1));
        assertEquals("Y", r1.getFieldValue(2));
    }

    // ---------------------------------------------------------------
    // hasTimeKey / getPKIndex
    // ---------------------------------------------------------------
    @Test
    @DisplayName("hasTimeKey returns false when no time key")
    void hasTimeKeyFalse() {
        Record r = new Record(ds);
        assertFalse(r.hasTimeKey());
    }

    @Test
    @DisplayName("getPKIndex returns null when no PK")
    void getPKIndexNull() {
        Record r = new Record(ds);
        assertNull(r.getPKIndex());
    }

    @Test
    @DisplayName("getPKIndex returns PK index when set")
    void getPKIndex() {
        ds.setPrimary(new String[]{"name"});
        Record r = new Record(ds);
        assertNotNull(r.getPKIndex());
        assertEquals(0, r.getPKIndex()[0]);
    }

    // ---------------------------------------------------------------
    // IComputeItem interface
    // ---------------------------------------------------------------
    @Test
    @DisplayName("getCurrent returns the record itself")
    void getCurrent() {
        Record r = new Record(ds);
        assertSame(r, r.getCurrent());
    }

    @Test
    @DisplayName("getCurrentIndex throws RuntimeException")
    void getCurrentIndex() {
        Record r = new Record(ds);
        assertThrows(RuntimeException.class, r::getCurrentIndex);
    }

    @Test
    @DisplayName("getCurrentSequence returns null")
    void getCurrentSequence() {
        Record r = new Record(ds);
        assertNull(r.getCurrentSequence());
    }

    // ---------------------------------------------------------------
    // toRecord
    // ---------------------------------------------------------------
    @Test
    @DisplayName("toRecord returns itself")
    void toRecord() {
        Record r = new Record(ds);
        assertSame(r, r.toRecord());
    }

    // ---------------------------------------------------------------
    // isSameDataStruct
    // ---------------------------------------------------------------
    @Test
    @DisplayName("isSameDataStruct checks identity of DataStruct")
    void isSameDataStruct() {
        Record r1 = new Record(ds);
        Record r2 = new Record(ds);
        assertTrue(r1.isSameDataStruct(r2));

        DataStruct ds2 = new DataStruct(new String[]{"name", "age", "city"});
        Record r3 = new Record(ds2);
        assertFalse(r1.isSameDataStruct(r3)); // different DataStruct instance
    }
}
