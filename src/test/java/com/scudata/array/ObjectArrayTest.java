package com.scudata.array;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;

/**
 * Tests for {@link ObjectArray} — a 1-based generic object array.
 * No signs array; null is stored directly in Object[] datas.
 */
@DisplayName("ObjectArray Tests")
public class ObjectArrayTest {

	private ObjectArray array;

	@BeforeEach
	void setUp() {
		array = new ObjectArray();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Constructors")
	class Constructors {

		@Test
		@DisplayName("Default constructor creates empty array")
		void defaultConstructor() {
			assertEquals(0, array.size());
		}

		@Test
		@DisplayName("Capacity constructor")
		void capacityConstructor() {
			ObjectArray a = new ObjectArray(50);
			assertEquals(0, a.size());
			for (int i = 0; i < 50; i++) {
				a.add(i);
			}
			assertEquals(50, a.size());
		}

		@Test
		@DisplayName("Object[] constructor uses all elements")
		void objectArrayConstructor() {
			Object[] values = {10, "hello", 3.14};
			ObjectArray a = new ObjectArray(values);
			assertEquals(3, a.size());
			assertEquals(10, a.get(1));
			assertEquals("hello", a.get(2));
			assertEquals(3.14, a.get(3));
		}

		@Test
		@DisplayName("Direct data + size constructor")
		void directDataConstructor() {
			Object[] datas = {null, "a", "b", "c", null}; // index 0 unused
			ObjectArray a = new ObjectArray(datas, 3);
			assertEquals(3, a.size());
			assertEquals("a", a.get(1));
			assertEquals("b", a.get(2));
			assertEquals("c", a.get(3));
		}
	}

	// -------------------------------------------------------------------------
	// add / push / get / set
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Element operations")
	class ElementOps {

		@Test
		@DisplayName("add any Object type")
		void addMixedTypes() {
			array.add(42);
			array.add("hello");
			array.add(3.14);
			array.add(null);
			assertEquals(4, array.size());
			assertEquals(42, array.get(1));
			assertEquals("hello", array.get(2));
			assertEquals(3.14, array.get(3));
			assertNull(array.get(4));
		}

		@Test
		@DisplayName("push adds without capacity check")
		void pushObject() {
			ObjectArray a = new ObjectArray(10);
			a.push("test");
			a.push(42);
			assertEquals(2, a.size());
			assertEquals("test", a.get(1));
		}

		@Test
		@DisplayName("pushNull adds null")
		void pushNull() {
			ObjectArray a = new ObjectArray(10);
			a.pushNull();
			assertEquals(1, a.size());
			assertNull(a.get(1));
		}

		@Test
		@DisplayName("set modifies element")
		void setElement() {
			array.add("old");
			array.set(1, "new");
			assertEquals("new", array.get(1));
		}

		@Test
		@DisplayName("set to null")
		void setNull() {
			array.add("hello");
			array.set(1, null);
			assertNull(array.get(1));
		}

		@Test
		@DisplayName("get returns raw object at index")
		void getReturnsObject() {
			array.add(42);
			Object v = array.get(1);
			assertTrue(v instanceof Integer);
			assertEquals(42, v);
		}
	}

	// -------------------------------------------------------------------------
	// size / count / clear
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Size and count")
	class SizeCount {

		@Test
		@DisplayName("count excludes false/null elements (uses Variant.isFalse)")
		void countExcludesFalseAndNull() {
			array.add(42);         // true
			array.add(null);       // false (null is false)
			array.add("hello");    // true
			array.add(Boolean.FALSE); // false
			// ObjectArray.count() decrements for Variant.isFalse elements
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("count with all true values equals size")
		void countAllTrue() {
			array.add(1);
			array.add("a");
			array.add(Boolean.TRUE);
			assertEquals(3, array.count());
		}

		@Test
		@DisplayName("clear removes all elements")
		void clear() {
			array.add(1);
			array.add(2);
			array.add(3);
			array.clear();
			assertEquals(0, array.size());
		}
	}

	// -------------------------------------------------------------------------
	// isNull / isTrue / isFalse
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Null and truth checks")
	class NullTruthChecks {

		@Test
		@DisplayName("isNull returns true only for null entries")
		void isNullCheck() {
			array.add("hello");
			array.add(null);
			assertFalse(array.isNull(1));
			assertTrue(array.isNull(2));
		}

		@Test
		@DisplayName("isTrue uses Variant.isTrue semantics")
		void isTrueCheck() {
			array.add(42);
			array.add(null);
			array.add(Boolean.FALSE);
			array.add("hello");
			assertTrue(array.isTrue(1));     // non-null, non-false
			assertFalse(array.isTrue(2));    // null is not true
			assertFalse(array.isTrue(3));    // Boolean.FALSE is not true
			assertTrue(array.isTrue(4));     // non-null string is true
		}

		@Test
		@DisplayName("isFalse uses Variant.isFalse semantics")
		void isFalseCheck() {
			array.add(42);
			array.add(null);
			array.add(Boolean.FALSE);
			assertFalse(array.isFalse(1));
			assertTrue(array.isFalse(2));
			assertTrue(array.isFalse(3));
		}
	}

	// -------------------------------------------------------------------------
	// contains
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Search operations")
	class SearchOps {

		@Test
		@DisplayName("contains finds existing element via Variant.isEquals")
		void containsExisting() {
			array.add(10);
			array.add(20);
			array.add(30);
			assertTrue(array.contains(20));
			assertFalse(array.contains(99));
		}

		@Test
		@DisplayName("contains null")
		void containsNull() {
			array.add(10);
			array.add(null);
			assertTrue(array.contains(null));
		}

		@Test
		@DisplayName("contains null returns false when no nulls")
		void containsNullNoNulls() {
			array.add(10);
			array.add(20);
			assertFalse(array.contains(null));
		}

		@Test
		@DisplayName("contains string")
		void containsString() {
			array.add("apple");
			array.add("banana");
			assertTrue(array.contains("banana"));
			assertFalse(array.contains("cherry"));
		}
	}

	// -------------------------------------------------------------------------
	// insert / remove
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Insert and remove")
	class InsertRemove {

		@Test
		@DisplayName("insert shifts elements right")
		void insertElement() {
			array.add("a");
			array.add("c");
			array.insert(2, "b");
			assertEquals(3, array.size());
			assertEquals("b", array.get(2));
			assertEquals("c", array.get(3));
		}

		@Test
		@DisplayName("remove shifts elements left and nulls last")
		void removeElement() {
			array.add("a");
			array.add("b");
			array.add("c");
			array.remove(2);
			assertEquals(2, array.size());
			assertEquals("a", array.get(1));
			assertEquals("c", array.get(2));
		}

		@Test
		@DisplayName("remove multiple positions")
		void removeMultiple() {
			array.add("a");
			array.add("b");
			array.add("c");
			array.add("d");
			array.remove(new int[]{1, 3});
			assertEquals(2, array.size());
			assertEquals("b", array.get(1));
			assertEquals("d", array.get(2));
		}

		@Test
		@DisplayName("removeLast removes last element")
		void removeLast() {
			array.add("a");
			array.add("b");
			array.removeLast();
			assertEquals(1, array.size());
			assertEquals("a", array.get(1));
		}
	}

	// -------------------------------------------------------------------------
	// sort / rvs
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Sort and reverse")
	class SortReverse {

		@Test
		@DisplayName("sort sorts elements")
		void sortElements() {
			array.add(30);
			array.add(10);
			array.add(20);
			array.sort();
			assertEquals(10, array.get(1));
			assertEquals(20, array.get(2));
			assertEquals(30, array.get(3));
		}

		@Test
		@DisplayName("rvs returns reversed copy")
		void reverse() {
			array.add("a");
			array.add("b");
			array.add("c");
			IArray reversed = array.rvs();
			assertEquals("c", reversed.get(1));
			assertEquals("b", reversed.get(2));
			assertEquals("a", reversed.get(3));
		}
	}

	// -------------------------------------------------------------------------
	// Aggregation: sum, average, min, max
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aggregation")
	class Aggregation {

		@Test
		@DisplayName("sum uses Variant.add")
		void sum() {
			array.add(10);
			array.add(20);
			array.add(30);
			Object result = array.sum();
			assertNotNull(result);
			assertEquals(60, ((Number) result).intValue());
		}

		@Test
		@DisplayName("sum of empty returns null")
		void sumEmpty() {
			assertNull(array.sum());
		}

		@Test
		@DisplayName("max finds maximum via Variant.compare")
		void max() {
			array.add(20);
			array.add(10);
			array.add(30);
			assertEquals(30, ((Number) array.max()).intValue());
		}

		@Test
		@DisplayName("max skips nulls")
		void maxWithNulls() {
			array.add(null);
			array.add(10);
			array.add(30);
			assertEquals(30, ((Number) array.max()).intValue());
		}

		@Test
		@DisplayName("max of empty returns null")
		void maxEmpty() {
			assertNull(array.max());
		}

		@Test
		@DisplayName("min via Variant.compare")
		void min() {
			array.add(20);
			array.add(10);
			array.add(30);
			assertEquals(10, ((Number) array.min()).intValue());
		}

		@Test
		@DisplayName("min skips nulls")
		void minWithNulls() {
			array.add(null);
			array.add(10);
			array.add(30);
			assertEquals(10, ((Number) array.min()).intValue());
		}

		@Test
		@DisplayName("min of all nulls returns null")
		void minAllNulls() {
			array.add(null);
			array.add(null);
			assertNull(array.min());
		}
	}

	// -------------------------------------------------------------------------
	// isPmt / hasRecord (ObjectArray-specific)
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Record checks — isPmt / hasRecord")
	class RecordChecks {

		@Test
		@DisplayName("hasRecord returns false for non-record elements")
		void hasRecordFalse() {
			array.add(1);
			array.add("hello");
			assertFalse(array.hasRecord());
		}

		@Test
		@DisplayName("hasRecord returns true when a Record is present")
		void hasRecordTrue() {
			DataStruct ds = new DataStruct(new String[]{"name", "age"});
			Record r = new Record(ds);
			array.add(r);
			assertTrue(array.hasRecord());
		}

		@Test
		@DisplayName("isPmt returns false for empty array")
		void isPmtEmpty() {
			assertFalse(array.isPmt(false));
			assertFalse(array.isPmt(true));
		}

		@Test
		@DisplayName("isPmt(false) returns true when all elements are Records or null")
		void isPmtNotPure() {
			DataStruct ds = new DataStruct(new String[]{"name"});
			array.add(new Record(ds));
			array.add(null);
			array.add(new Record(ds));
			assertTrue(array.isPmt(false));
		}

		@Test
		@DisplayName("isPmt(false) returns false when non-record non-null present")
		void isPmtFalseWithMixed() {
			DataStruct ds = new DataStruct(new String[]{"name"});
			array.add(new Record(ds));
			array.add("not a record");
			assertFalse(array.isPmt(false));
		}

		@Test
		@DisplayName("isPmt(true) checks all records have compatible DataStruct")
		void isPmtPure() {
			DataStruct ds = new DataStruct(new String[]{"name", "age"});
			array.add(new Record(ds));
			array.add(new Record(ds));
			assertTrue(array.isPmt(true));
		}

		@Test
		@DisplayName("isPmt(true) returns false for different DataStructs")
		void isPmtPureDifferentDs() {
			DataStruct ds1 = new DataStruct(new String[]{"name"});
			DataStruct ds2 = new DataStruct(new String[]{"id"});
			array.add(new Record(ds1));
			array.add(new Record(ds2));
			assertFalse(array.isPmt(true));
		}

		@Test
		@DisplayName("isPmt(true) returns false if first element is not a Record")
		void isPmtPureFirstNotRecord() {
			array.add("not a record");
			assertFalse(array.isPmt(true));
		}
	}

	// -------------------------------------------------------------------------
	// Utility: dup, addAll, ensureCapacity, trimToSize, etc.
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Utility operations")
	class UtilOps {

		@Test
		@DisplayName("dup creates independent copy")
		void dup() {
			array.add("hello");
			array.add(42);
			IArray copy = array.dup();
			assertEquals(2, copy.size());
			assertEquals("hello", copy.get(1));
			assertEquals(42, copy.get(2));

			array.set(1, "changed");
			assertEquals("hello", copy.get(1)); // independent
		}

		@Test
		@DisplayName("addAll(IArray) from ObjectArray")
		void addAllIArray() {
			array.add(1);
			ObjectArray other = new ObjectArray();
			other.add(2);
			other.add(3);
			array.addAll(other);
			assertEquals(3, array.size());
			assertEquals(2, array.get(2));
			assertEquals(3, array.get(3));
		}

		@Test
		@DisplayName("addAll(Object[]) from raw array")
		void addAllObjectArray() {
			array.add(1);
			Object[] more = {2, 3, 4};
			array.addAll(more);
			assertEquals(4, array.size());
			assertEquals(3, array.get(3));
		}

		@Test
		@DisplayName("ensureCapacity grows internal array")
		void ensureCapacity() {
			array.ensureCapacity(500);
			for (int i = 0; i < 500; i++) {
				array.add(i);
			}
			assertEquals(500, array.size());
		}

		@Test
		@DisplayName("trimToSize reduces internal array")
		void trimToSize() {
			array.ensureCapacity(500);
			array.add(1);
			array.add(2);
			array.trimToSize();
			assertEquals(2, array.size());
			assertEquals(1, array.get(1));
		}

		@Test
		@DisplayName("newInstance returns ObjectArray")
		void newInstance() {
			IArray instance = array.newInstance(10);
			assertTrue(instance instanceof ObjectArray);
			assertEquals(0, instance.size());
		}

		@Test
		@DisplayName("ifn returns first non-null element")
		void ifn() {
			array.add(null);
			array.add(42);
			array.add(99);
			assertEquals(42, array.ifn());
		}

		@Test
		@DisplayName("ifn returns null when all null")
		void ifnAllNull() {
			array.add(null);
			array.add(null);
			assertNull(array.ifn());
		}

		@Test
		@DisplayName("get(int, int) range sub-array")
		void getRange() {
			array.add("a");
			array.add("b");
			array.add("c");
			array.add("d");
			IArray sub = array.get(2, 4); // start inclusive, end exclusive
			assertEquals(2, sub.size());
			assertEquals("b", sub.get(1));
			assertEquals("c", sub.get(2));
		}
	}
}
