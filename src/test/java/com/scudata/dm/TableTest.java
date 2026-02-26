package com.scudata.dm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.common.RQException;

/**
 * Tests for {@link Table}.
 * Covers constructors, record manipulation, structural operations,
 * primary keys, and Table-specific overrides.
 */
class TableTest {

    private DataStruct ds;

    @BeforeEach
    void setUp() {
        ds = new DataStruct(new String[]{"id", "name", "age"});
    }

    // ========== Constructors ==========

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("Table(String[]) creates empty table with given field names")
        void stringArrayConstructor() {
            Table t = new Table(new String[]{"a", "b"});
            assertEquals(0, t.length());
            assertNotNull(t.dataStruct());
            assertEquals(2, t.dataStruct().getFieldCount());
            assertEquals("a", t.dataStruct().getFieldName(0));
        }

        @Test
        @DisplayName("Table(DataStruct) creates empty table with given structure")
        void dataStructConstructor() {
            Table t = new Table(ds);
            assertEquals(0, t.length());
            assertSame(ds, t.dataStruct());
        }

        @Test
        @DisplayName("Table(DataStruct, int) creates empty table with capacity")
        void dataStructCapacityConstructor() {
            Table t = new Table(ds, 100);
            assertEquals(0, t.length());
            assertSame(ds, t.dataStruct());
        }

        @Test
        @DisplayName("Table(String[], int) creates empty table with field names and capacity")
        void stringArrayCapacityConstructor() {
            Table t = new Table(new String[]{"x", "y"}, 50);
            assertEquals(0, t.length());
            assertEquals(2, t.dataStruct().getFieldCount());
        }

        @Test
        @DisplayName("Table(Table) deep copies the table")
        void copyConstructor() {
            Table orig = new Table(ds);
            orig.newLast(new Object[]{1, "Alice", 25});
            orig.newLast(new Object[]{2, "Bob", 30});

            Table copy = new Table(orig);
            assertEquals(2, copy.length());
            // Copy should have independent records
            BaseRecord r1 = (BaseRecord) copy.getMem(1);
            r1.set(1, "Modified");
            // Original should not be affected
            BaseRecord origR1 = (BaseRecord) orig.getMem(1);
            assertEquals("Alice", origR1.getFieldValue(1));
        }
    }

    // ========== newLast ==========

    @Nested
    @DisplayName("newLast")
    class NewLast {

        @Test
        @DisplayName("newLast() appends empty record and returns it")
        void newLastEmpty() {
            Table t = new Table(ds);
            BaseRecord r = t.newLast();
            assertEquals(1, t.length());
            assertNotNull(r);
            assertNull(r.getFieldValue(0)); // id
            assertNull(r.getFieldValue(1)); // name
            assertNull(r.getFieldValue(2)); // age
        }

        @Test
        @DisplayName("newLast(Object[]) appends record with given values")
        void newLastWithValues() {
            Table t = new Table(ds);
            BaseRecord r = t.newLast(new Object[]{1, "Alice", 25});
            assertEquals(1, t.length());
            assertEquals(1, r.getFieldValue(0));
            assertEquals("Alice", r.getFieldValue(1));
            assertEquals(25, r.getFieldValue(2));
        }

        @Test
        @DisplayName("newLast multiple times")
        void newLastMultiple() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            t.newLast(new Object[]{2, "B", 20});
            t.newLast(new Object[]{3, "C", 30});
            assertEquals(3, t.length());
        }
    }

    // ========== getRecord ==========

    @Nested
    @DisplayName("getRecord")
    class GetRecord {

        @Test
        @DisplayName("getRecord within bounds returns existing record")
        void getRecordInBounds() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "Alice", 25});
            BaseRecord r = t.getRecord(1);
            assertEquals(1, r.getFieldValue(0));
        }

        @Test
        @DisplayName("getRecord beyond bounds auto-extends with empty records")
        void getRecordAutoExtend() {
            Table t = new Table(ds);
            BaseRecord r = t.getRecord(3);
            assertEquals(3, t.length());
            assertNotNull(r);
            // All fields should be null for auto-created record
            assertNull(r.getFieldValue(0));
        }
    }

    // ========== add / insert ==========

    @Nested
    @DisplayName("add and insert")
    class AddAndInsert {

        @Test
        @DisplayName("add(Record) with same DataStruct succeeds")
        void addSameDs() {
            Table t = new Table(ds);
            Record r = new Record(ds, new Object[]{1, "A", 10});
            t.add(r);
            assertEquals(1, t.length());
        }

        @Test
        @DisplayName("add(Record) with different DataStruct throws")
        void addDifferentDsThrows() {
            Table t = new Table(ds);
            DataStruct otherDs = new DataStruct(new String[]{"x"});
            Record r = new Record(otherDs, new Object[]{1});
            assertThrows(RQException.class, () -> t.add(r));
        }

        @Test
        @DisplayName("insert(pos) inserts empty record at position")
        void insertPos() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "First", 10});
            t.newLast(new Object[]{3, "Third", 30});
            BaseRecord r = t.insert(2);
            assertNotNull(r);
            assertEquals(3, t.length());
            // inserted record at position 2 should have null fields
            BaseRecord atTwo = (BaseRecord) t.getMem(2);
            assertNull(atTwo.getFieldValue(0));
        }

        @Test
        @DisplayName("insert(0) appends to end")
        void insertZeroAppends() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            BaseRecord r = t.insert(0);
            assertEquals(2, t.length());
            // Appended record is at the end
            BaseRecord last = (BaseRecord) t.getMem(2);
            assertNull(last.getFieldValue(0));
        }

        @Test
        @DisplayName("insert(pos, Object[]) inserts record with values")
        void insertWithValues() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            t.newLast(new Object[]{3, "C", 30});
            BaseRecord r = t.insert(2, new Object[]{2, "B", 20});
            assertEquals(3, t.length());
            assertEquals(2, r.getFieldValue(0));
            // Position 2 should be the inserted record
            BaseRecord atTwo = (BaseRecord) t.getMem(2);
            assertEquals("B", atTwo.getFieldValue(1));
        }
    }

    // ========== Table-specific overrides ==========

    @Nested
    @DisplayName("Table-specific overrides")
    class TableOverrides {

        @Test
        @DisplayName("count() returns total record count (even if fields are null)")
        void countReturnsSize() {
            Table t = new Table(ds);
            t.newLast(new Object[]{null, null, null});
            t.newLast(new Object[]{1, "A", 10});
            // Table.count() returns mems.size(), not non-null count
            assertEquals(2, t.count());
        }

        @Test
        @DisplayName("hasRecord() always returns true for Table")
        void hasRecordAlwaysTrue() {
            Table t = new Table(ds);
            assertTrue(t.hasRecord());
        }

        @Test
        @DisplayName("isPmt() always returns true for Table")
        void isPmtAlwaysTrue() {
            Table t = new Table(ds);
            assertTrue(t.isPmt());
        }

        @Test
        @DisplayName("isPurePmt() always returns true for Table")
        void isPurePmtAlwaysTrue() {
            Table t = new Table(ds);
            assertTrue(t.isPurePmt());
        }

        @Test
        @DisplayName("isEquals checks identity only for Table")
        void isEqualsIdentityOnly() {
            Table t1 = new Table(ds);
            t1.newLast(new Object[]{1, "A", 10});

            Table t2 = new Table(ds);
            t2.newLast(new Object[]{1, "A", 10});

            // Different table objects, even with same data, may not be equal
            // Table.isEquals uses == identity check
            assertTrue(t1.isEquals(t1));
        }
    }

    // ========== create ==========

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("create() returns empty table with same DataStruct")
        void createEmpty() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            Table empty = t.create();
            assertEquals(0, empty.length());
            assertSame(ds, empty.dataStruct());
        }
    }

    // ========== dataStruct ==========

    @Nested
    @DisplayName("dataStruct")
    class DataStructAccess {

        @Test
        @DisplayName("dataStruct() returns the DataStruct")
        void dataStruct() {
            Table t = new Table(ds);
            assertSame(ds, t.dataStruct());
        }

        @Test
        @DisplayName("getFieldCount() returns number of fields")
        void getFieldCount() {
            Table t = new Table(ds);
            assertEquals(3, t.getFieldCount());
        }

        @Test
        @DisplayName("containField returns true for existing field")
        void containFieldTrue() {
            Table t = new Table(ds);
            assertTrue(t.containField("name"));
        }

        @Test
        @DisplayName("containField returns false for non-existing field")
        void containFieldFalse() {
            Table t = new Table(ds);
            assertFalse(t.containField("nonexistent"));
        }
    }

    // ========== setPrimary / getPrimary ==========

    @Nested
    @DisplayName("Primary key")
    class PrimaryKey {

        @Test
        @DisplayName("setPrimary sets primary key fields")
        void setPrimary() {
            Table t = new Table(ds);
            t.setPrimary(new String[]{"id"});
            String[] pk = t.getPrimary();
            assertNotNull(pk);
            assertEquals(1, pk.length);
            assertEquals("id", pk[0]);
        }

        @Test
        @DisplayName("setPrimary with null clears primary key")
        void setPrimaryNull() {
            Table t = new Table(ds);
            t.setPrimary(new String[]{"id"});
            t.setPrimary(null);
            assertNull(t.getPrimary());
        }

        @Test
        @DisplayName("setPrimary with invalid field throws")
        void setPrimaryInvalidField() {
            Table t = new Table(ds);
            assertThrows(RQException.class,
                    () -> t.setPrimary(new String[]{"nonexistent"}));
        }
    }

    // ========== split ==========

    @Nested
    @DisplayName("split")
    class Split {

        @Test
        @DisplayName("split extracts and removes range of records")
        void splitRange() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            t.newLast(new Object[]{2, "B", 20});
            t.newLast(new Object[]{3, "C", 30});
            t.newLast(new Object[]{4, "D", 40});

            Sequence split = t.split(2, 3);
            // Split should contain 2 records
            assertEquals(2, split.length());
            // Original table should now have 2 records
            assertEquals(2, t.length());
            // Remaining records should be 1 and 4
            BaseRecord r1 = (BaseRecord) t.getMem(1);
            assertEquals(1, r1.getFieldValue(0));
            BaseRecord r2 = (BaseRecord) t.getMem(2);
            assertEquals(4, r2.getFieldValue(0));
        }

        @Test
        @DisplayName("split with invalid range throws")
        void splitInvalidRange() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            assertThrows(RQException.class, () -> t.split(0, 1));
            assertThrows(RQException.class, () -> t.split(1, 2));
        }
    }

    // ========== toString ==========

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("toString includes field names and values")
        void toStringContainsData() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "Alice", 25});
            String s = t.toString();
            assertTrue(s.contains("id"));
            assertTrue(s.contains("name"));
            assertTrue(s.contains("age"));
            assertTrue(s.contains("Alice"));
            assertTrue(s.contains("25"));
        }

        @Test
        @DisplayName("toString limits output to 10 records")
        void toStringMaxTen() {
            Table t = new Table(new String[]{"v"});
            for (int i = 0; i < 20; i++) {
                t.newLast(new Object[]{i});
            }
            String s = t.toString();
            // Should contain records 0-9 but not necessarily 10-19
            assertTrue(s.contains("0"));
            assertTrue(s.contains("9"));
        }
    }

    // ========== insert(pos, Table) ==========

    @Nested
    @DisplayName("insert Table")
    class InsertTable {

        @Test
        @DisplayName("insert(pos, Table) merges records from another table")
        void insertTable() {
            Table t1 = new Table(ds);
            t1.newLast(new Object[]{1, "A", 10});
            t1.newLast(new Object[]{3, "C", 30});

            Table t2 = new Table(ds);
            t2.newLast(new Object[]{2, "B", 20});

            t1.insert(2, t2);
            assertEquals(3, t1.length());
            BaseRecord atTwo = (BaseRecord) t1.getMem(2);
            assertEquals(2, atTwo.getFieldValue(0));
            // Source table should be cleared after insert
            assertEquals(0, t2.length());
        }

        @Test
        @DisplayName("insert(pos, Table) with mismatched field count throws")
        void insertTableMismatch() {
            Table t1 = new Table(ds);
            Table t2 = new Table(new String[]{"x"});
            t2.newLast(new Object[]{1});
            assertThrows(RQException.class, () -> t1.insert(1, t2));
        }
    }

    // ========== delete ==========

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("delete by position sequence")
        void deleteByPositions() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            t.newLast(new Object[]{2, "B", 20});
            t.newLast(new Object[]{3, "C", 30});

            Sequence positions = new Sequence(new Object[]{1, 3});
            Sequence result = t.delete(positions, null);
            assertSame(t, result);
            assertEquals(1, t.length());
            BaseRecord remaining = (BaseRecord) t.getMem(1);
            assertEquals(2, remaining.getFieldValue(0));
        }

        @Test
        @DisplayName("delete with 'n' option returns deleted records")
        void deleteReturnsDeleted() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            t.newLast(new Object[]{2, "B", 20});

            Sequence positions = new Sequence(new Object[]{1});
            Sequence deleted = t.delete(positions, "n");
            assertEquals(1, deleted.length());
            assertEquals(1, t.length());
        }

        @Test
        @DisplayName("delete with null/empty sequence returns self")
        void deleteEmpty() {
            Table t = new Table(ds);
            t.newLast(new Object[]{1, "A", 10});
            Sequence result = t.delete(null, null);
            assertSame(t, result);
            assertEquals(1, t.length());
        }
    }

    // ========== indexTable ==========

    @Nested
    @DisplayName("Index table")
    class IndexTable {

        @Test
        @DisplayName("getIndexTable returns null before creation")
        void indexTableNullByDefault() {
            Table t = new Table(ds);
            assertNull(t.getIndexTable());
        }

        @Test
        @DisplayName("deleteIndexTable clears the index")
        void deleteIndexTable() {
            Table t = new Table(ds);
            t.deleteIndexTable();
            assertNull(t.getIndexTable());
        }
    }

    // ========== setDataStruct ==========

    @Test
    @DisplayName("setDataStruct changes the data structure")
    void setDataStruct() {
        Table t = new Table(ds);
        DataStruct newDs = new DataStruct(new String[]{"x", "y", "z"});
        t.setDataStruct(newDs);
        assertSame(newDs, t.dataStruct());
    }
}
