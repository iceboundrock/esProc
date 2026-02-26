package com.scudata.array;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StringArray} — a 1-based string array. Nulls are stored directly
 * (no separate signs array); null is just a null entry in String[] datas.
 */
@DisplayName("StringArray Tests")
public class StringArrayTest {

	private StringArray array;

	@BeforeEach
	void setUp() {
		array = new StringArray();
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
			StringArray a = new StringArray(50);
			assertEquals(0, a.size());
			for (int i = 0; i < 50; i++) {
				a.add("s" + i);
			}
			assertEquals(50, a.size());
		}

		@Test
		@DisplayName("Direct data constructor")
		void directDataConstructor() {
			String[] datas = {null, "a", "b", "c"};
			StringArray a = new StringArray(datas, 3);
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
		@DisplayName("add String values")
		void addString() {
			array.add("hello");
			array.add("world");
			assertEquals(2, array.size());
			assertEquals("hello", array.get(1));
			assertEquals("world", array.get(2));
		}

		@Test
		@DisplayName("add null")
		void addNull() {
			array.add("hello");
			array.add(null);
			array.add("world");
			assertNull(array.get(2));
		}

		@Test
		@DisplayName("add incompatible type throws exception")
		void addIncompatible() {
			assertThrows(Exception.class, () -> array.add(42));
		}

		@Test
		@DisplayName("push adds without capacity check")
		void pushString() {
			StringArray a = new StringArray(10);
			a.push("a");
			a.push("b");
			assertEquals(2, a.size());
			assertEquals("a", a.get(1));
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
	}

	// -------------------------------------------------------------------------
	// size / count / clear
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Size and count")
	class SizeCount {

		@Test
		@DisplayName("count excludes nulls")
		void countExcludesNull() {
			array.add("a");
			array.add(null);
			array.add("c");
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("count with no nulls equals size")
		void countNoNulls() {
			array.add("a");
			array.add("b");
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("clear resets array")
		void clear() {
			array.add("a");
			array.add("b");
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
		@DisplayName("isNull for null and non-null")
		void isNullCheck() {
			array.add("hello");
			array.add(null);
			assertFalse(array.isNull(1));
			assertTrue(array.isNull(2));
		}

		@Test
		@DisplayName("isTrue for non-null string")
		void isTrueCheck() {
			array.add("hello");
			array.add(null);
			assertTrue(array.isTrue(1));
			assertFalse(array.isTrue(2));
		}

		@Test
		@DisplayName("isFalse for null")
		void isFalseCheck() {
			array.add("hello");
			array.add(null);
			assertFalse(array.isFalse(1));
			assertTrue(array.isFalse(2));
		}
	}

	// -------------------------------------------------------------------------
	// contains
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Search operations")
	class SearchOps {

		@Test
		@DisplayName("contains finds existing string")
		void containsExisting() {
			array.add("apple");
			array.add("banana");
			array.add("cherry");
			assertTrue(array.contains("banana"));
			assertFalse(array.contains("grape"));
		}

		@Test
		@DisplayName("contains null")
		void containsNull() {
			array.add("a");
			array.add(null);
			assertTrue(array.contains(null));
		}

		@Test
		@DisplayName("contains null returns false when no nulls")
		void containsNullNoNulls() {
			array.add("a");
			assertFalse(array.contains(null));
		}
	}

	// -------------------------------------------------------------------------
	// Static compare / isEquals
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Static comparison methods")
	class StaticComparison {

		@Test
		@DisplayName("compare two strings")
		void compareStrings() {
			assertTrue(StringArray.compare("a", "b") < 0);
			assertEquals(0, StringArray.compare("hello", "hello"));
			assertTrue(StringArray.compare("z", "a") > 0);
		}

		@Test
		@DisplayName("compare with nulls — null is smallest")
		void compareWithNull() {
			assertTrue(StringArray.compare(null, "a") < 0);
			assertTrue(StringArray.compare("a", null) > 0);
			assertEquals(0, StringArray.compare(null, null));
		}

		@Test
		@DisplayName("isEquals two strings")
		void isEqualsStrings() {
			assertTrue(StringArray.isEquals("hello", "hello"));
			assertFalse(StringArray.isEquals("hello", "world"));
		}

		@Test
		@DisplayName("isEquals with null")
		void isEqualsNull() {
			assertFalse(StringArray.isEquals(null, "hello"));
			assertFalse(StringArray.isEquals("hello", null));
			assertTrue(StringArray.isEquals(null, null));
		}

		@Test
		@DisplayName("isEquals same reference")
		void isEqualsSameReference() {
			String s = "test";
			assertTrue(StringArray.isEquals(s, s));
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
		@DisplayName("remove shifts elements left")
		void removeElement() {
			array.add("a");
			array.add("b");
			array.add("c");
			array.remove(2);
			assertEquals(2, array.size());
			assertEquals("c", array.get(2));
		}
	}

	// -------------------------------------------------------------------------
	// sort / rvs
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Sort and reverse")
	class SortReverse {

		@Test
		@DisplayName("sort ascending")
		void sortAscending() {
			array.add("cherry");
			array.add("apple");
			array.add("banana");
			array.sort();
			assertEquals("apple", array.get(1));
			assertEquals("banana", array.get(2));
			assertEquals("cherry", array.get(3));
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
	// Aggregation (string-specific: sum concatenates, max/min use string compare)
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aggregation")
	class Aggregation {

		@Test
		@DisplayName("max string")
		void max() {
			array.add("banana");
			array.add("apple");
			array.add("cherry");
			assertEquals("cherry", array.max());
		}

		@Test
		@DisplayName("max skips nulls")
		void maxWithNulls() {
			array.add(null);
			array.add("apple");
			array.add("cherry");
			assertEquals("cherry", array.max());
		}

		@Test
		@DisplayName("max of empty returns null")
		void maxEmpty() {
			assertNull(array.max());
		}

		@Test
		@DisplayName("min string")
		void min() {
			array.add("banana");
			array.add("apple");
			array.add("cherry");
			assertEquals("apple", array.min());
		}

		@Test
		@DisplayName("min skips nulls")
		void minWithNulls() {
			array.add(null);
			array.add("banana");
			array.add("apple");
			assertEquals("apple", array.min());
		}
	}

	// -------------------------------------------------------------------------
	// Utility
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Utility operations")
	class UtilOps {

		@Test
		@DisplayName("dup creates independent copy")
		void dup() {
			array.add("hello");
			array.add(null);
			IArray copy = array.dup();
			assertEquals(2, copy.size());
			assertEquals("hello", copy.get(1));
			assertNull(copy.get(2));

			array.set(1, "changed");
			assertEquals("hello", copy.get(1)); // independent
		}

		@Test
		@DisplayName("addAll from another StringArray")
		void addAll() {
			array.add("a");
			StringArray other = new StringArray();
			other.add("b");
			other.add("c");
			array.addAll(other);
			assertEquals(3, array.size());
			assertEquals("b", array.get(2));
		}

		@Test
		@DisplayName("hasRecord returns false")
		void hasRecord() {
			array.add("a");
			assertFalse(array.hasRecord());
		}

		@Test
		@DisplayName("isPmt returns false")
		void isPmt() {
			array.add("a");
			assertFalse(array.isPmt(false));
		}

		@Test
		@DisplayName("newInstance returns new StringArray")
		void newInstance() {
			IArray instance = array.newInstance(10);
			assertTrue(instance instanceof StringArray);
			assertEquals(0, instance.size());
		}

		@Test
		@DisplayName("ensureCapacity and trimToSize")
		void ensureCapacityAndTrim() {
			array.ensureCapacity(500);
			array.add("a");
			array.add("b");
			array.trimToSize();
			assertEquals(2, array.size());
			assertEquals("a", array.get(1));
		}
	}
}
