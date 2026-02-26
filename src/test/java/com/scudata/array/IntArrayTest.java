package com.scudata.array;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IntArray} — a 1-based integer array with null tracking via boolean[] signs.
 */
@DisplayName("IntArray Tests")
public class IntArrayTest {

	private IntArray array;

	@BeforeEach
	void setUp() {
		array = new IntArray();
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
			IntArray a = new IntArray();
			assertEquals(0, a.size());
		}

		@Test
		@DisplayName("Capacity constructor creates empty array with given capacity")
		void capacityConstructor() {
			IntArray a = new IntArray(100);
			assertEquals(0, a.size());
			// Should not throw when adding up to capacity
			for (int i = 0; i < 100; i++) {
				a.add(i);
			}
			assertEquals(100, a.size());
		}

		@Test
		@DisplayName("Range constructor creates array from start to end inclusive")
		void rangeConstructor() {
			IntArray a = new IntArray(3, 7);
			assertEquals(5, a.size());
			assertEquals(3, a.get(1));
			assertEquals(4, a.get(2));
			assertEquals(5, a.get(3));
			assertEquals(6, a.get(4));
			assertEquals(7, a.get(5));
		}

		@Test
		@DisplayName("Range constructor with single element")
		void rangeConstructorSingleElement() {
			IntArray a = new IntArray(5, 5);
			assertEquals(1, a.size());
			assertEquals(5, a.get(1));
		}

		@Test
		@DisplayName("Direct data constructor")
		void directDataConstructor() {
			int[] datas = {0, 10, 20, 30};
			boolean[] signs = {false, false, true, false};
			IntArray a = new IntArray(datas, signs, 3);
			assertEquals(3, a.size());
			assertEquals(10, a.get(1));
			assertNull(a.get(2)); // signs[2] = true means null
			assertEquals(30, a.get(3));
		}
	}

	// -------------------------------------------------------------------------
	// add / push / get / set
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Element operations")
	class ElementOps {

		@Test
		@DisplayName("add Integer values")
		void addInteger() {
			array.add(10);
			array.add(20);
			array.add(30);
			assertEquals(3, array.size());
			assertEquals(10, array.get(1));
			assertEquals(20, array.get(2));
			assertEquals(30, array.get(3));
		}

		@Test
		@DisplayName("add null creates signs array")
		void addNull() {
			array.add(10);
			array.add(null);
			array.add(30);
			assertEquals(3, array.size());
			assertEquals(10, array.get(1));
			assertNull(array.get(2));
			assertEquals(30, array.get(3));
		}

		@Test
		@DisplayName("add incompatible type throws RQException")
		void addIncompatibleType() {
			assertThrows(Exception.class, () -> array.add("hello"));
		}

		@Test
		@DisplayName("push adds without capacity check")
		void pushInt() {
			IntArray a = new IntArray(10);
			a.push(42);
			a.push(99);
			assertEquals(2, a.size());
			assertEquals(42, a.get(1));
			assertEquals(99, a.get(2));
		}

		@Test
		@DisplayName("set modifies existing element")
		void setElement() {
			array.add(10);
			array.add(20);
			array.set(1, 99);
			assertEquals(99, array.get(1));
		}

		@Test
		@DisplayName("set to null creates signs if needed")
		void setNull() {
			array.add(10);
			array.add(20);
			array.set(1, null);
			assertNull(array.get(1));
			assertEquals(20, array.get(2));
		}

		@Test
		@DisplayName("set non-null clears sign flag")
		void setNonNullClearsSign() {
			array.add(null);
			assertNull(array.get(1));
			array.set(1, 42);
			assertEquals(42, array.get(1));
		}

		@Test
		@DisplayName("get returns Integer for non-null, null for null")
		void getReturnsCorrectTypes() {
			array.add(10);
			array.add(null);
			Object v1 = array.get(1);
			Object v2 = array.get(2);
			assertTrue(v1 instanceof Integer);
			assertEquals(10, ((Integer) v1).intValue());
			assertNull(v2);
		}

		@Test
		@DisplayName("getInt returns raw int value")
		void getInt() {
			array.add(42);
			assertEquals(42, array.getInt(1));
		}
	}

	// -------------------------------------------------------------------------
	// size / count / clear
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Size and count")
	class SizeCount {

		@Test
		@DisplayName("size returns element count")
		void sizeReturnsCount() {
			assertEquals(0, array.size());
			array.add(1);
			array.add(2);
			assertEquals(2, array.size());
		}

		@Test
		@DisplayName("count excludes null elements")
		void countExcludesNull() {
			array.add(1);
			array.add(null);
			array.add(3);
			array.add(null);
			assertEquals(4, array.size());
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("count with no nulls equals size")
		void countNoNulls() {
			array.add(1);
			array.add(2);
			array.add(3);
			assertEquals(3, array.count());
		}

		@Test
		@DisplayName("clear resets array")
		void clearResetsArray() {
			array.add(1);
			array.add(null);
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
		@DisplayName("isNull returns true for null elements")
		void isNullCheck() {
			array.add(1);
			array.add(null);
			assertFalse(array.isNull(1));
			assertTrue(array.isNull(2));
		}

		@Test
		@DisplayName("isTrue returns true for non-null non-zero")
		void isTrueCheck() {
			array.add(42);
			array.add(null);
			assertTrue(array.isTrue(1));
			assertFalse(array.isTrue(2));
		}

		@Test
		@DisplayName("isFalse returns true for null elements")
		void isFalseCheck() {
			array.add(42);
			array.add(null);
			assertFalse(array.isFalse(1));
			assertTrue(array.isFalse(2));
		}
	}

	// -------------------------------------------------------------------------
	// contains / binarySearch
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Search operations")
	class SearchOps {

		@Test
		@DisplayName("contains finds existing element")
		void containsExisting() {
			array.add(10);
			array.add(20);
			array.add(30);
			assertTrue(array.contains(20));
			assertFalse(array.contains(99));
		}

		@Test
		@DisplayName("contains finds null when signs present")
		void containsNull() {
			array.add(10);
			array.add(null);
			assertTrue(array.contains(null));
		}

		@Test
		@DisplayName("contains returns false for null when no signs")
		void containsNullNoSigns() {
			array.add(10);
			array.add(20);
			assertFalse(array.contains(null));
		}

		@Test
		@DisplayName("contains with non-number returns false")
		void containsNonNumber() {
			array.add(10);
			assertFalse(array.contains("hello"));
		}

		@Test
		@DisplayName("binarySearch finds element in sorted array")
		void binarySearchFinds() {
			IntArray sorted = new IntArray(1, 10);
			assertEquals(5, sorted.binarySearch(5));
		}

		@Test
		@DisplayName("binarySearch returns negative for missing element")
		void binarySearchMissing() {
			IntArray sorted = new IntArray(1, 10);
			assertTrue(sorted.binarySearch(15) < 0);
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
			array.add(10);
			array.add(30);
			array.insert(2, 20);
			assertEquals(3, array.size());
			assertEquals(10, array.get(1));
			assertEquals(20, array.get(2));
			assertEquals(30, array.get(3));
		}

		@Test
		@DisplayName("remove shifts elements left")
		void removeElement() {
			array.add(10);
			array.add(20);
			array.add(30);
			array.remove(2);
			assertEquals(2, array.size());
			assertEquals(10, array.get(1));
			assertEquals(30, array.get(2));
		}

		@Test
		@DisplayName("remove multiple positions")
		void removeMultiple() {
			array.add(10);
			array.add(20);
			array.add(30);
			array.add(40);
			array.remove(new int[]{1, 3}); // remove positions 1 and 3
			assertEquals(2, array.size());
			assertEquals(20, array.get(1));
			assertEquals(40, array.get(2));
		}
	}

	// -------------------------------------------------------------------------
	// sort / rvs
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Sort and reverse")
	class SortReverse {

		@Test
		@DisplayName("sort arranges elements in ascending order")
		void sortAscending() {
			array.add(30);
			array.add(10);
			array.add(20);
			array.sort();
			assertEquals(10, array.get(1));
			assertEquals(20, array.get(2));
			assertEquals(30, array.get(3));
		}

		@Test
		@DisplayName("sort with nulls puts nulls first")
		void sortWithNulls() {
			array.add(30);
			array.add(null);
			array.add(10);
			array.sort();
			assertTrue(array.isNull(1));
			assertEquals(10, array.get(2));
			assertEquals(30, array.get(3));
		}

		@Test
		@DisplayName("rvs returns reversed copy")
		void reverseArray() {
			array.add(10);
			array.add(20);
			array.add(30);
			IArray reversed = array.rvs();
			assertEquals(3, reversed.size());
			assertEquals(30, reversed.get(1));
			assertEquals(20, reversed.get(2));
			assertEquals(10, reversed.get(3));
		}

		@Test
		@DisplayName("rvs with nulls reverses correctly")
		void reverseWithNulls() {
			array.add(10);
			array.add(null);
			array.add(30);
			IArray reversed = array.rvs();
			assertEquals(30, reversed.get(1));
			assertNull(reversed.get(2));
			assertEquals(10, reversed.get(3));
		}
	}

	// -------------------------------------------------------------------------
	// Aggregation: sum, average, min, max
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aggregation operations")
	class Aggregation {

		@Test
		@DisplayName("sum of all elements")
		void sumAll() {
			array.add(10);
			array.add(20);
			array.add(30);
			Object result = array.sum();
			// IntArray.sum() returns long
			assertEquals(60L, ((Number) result).longValue());
		}

		@Test
		@DisplayName("sum skips null elements")
		void sumWithNulls() {
			array.add(10);
			array.add(null);
			array.add(30);
			Object result = array.sum();
			assertEquals(40L, ((Number) result).longValue());
		}

		@Test
		@DisplayName("sum of all nulls returns null")
		void sumAllNulls() {
			array.add(null);
			array.add(null);
			assertNull(array.sum());
		}

		@Test
		@DisplayName("average of elements")
		void average() {
			array.add(10);
			array.add(20);
			array.add(30);
			Object result = array.average();
			assertEquals(20.0, ((Number) result).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("average skips nulls")
		void averageWithNulls() {
			array.add(10);
			array.add(null);
			array.add(30);
			Object result = array.average();
			assertEquals(20.0, ((Number) result).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("average of empty returns null")
		void averageEmpty() {
			assertNull(array.average());
		}

		@Test
		@DisplayName("max of elements")
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
		@DisplayName("min of elements")
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
	// dup / ensureCapacity / trimToSize / addAll
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Utility operations")
	class UtilOps {

		@Test
		@DisplayName("dup creates independent copy")
		void dupCreatesIndependentCopy() {
			array.add(10);
			array.add(20);
			IArray copy = array.dup();
			assertEquals(2, copy.size());
			assertEquals(10, copy.get(1));
			assertEquals(20, copy.get(2));

			// Modifying original doesn't affect copy
			array.set(1, 99);
			assertEquals(10, copy.get(1));
		}

		@Test
		@DisplayName("dup with nulls preserves signs")
		void dupWithNulls() {
			array.add(10);
			array.add(null);
			IArray copy = array.dup();
			assertNull(copy.get(2));
		}

		@Test
		@DisplayName("ensureCapacity allows adding many elements")
		void ensureCapacity() {
			array.ensureCapacity(1000);
			for (int i = 0; i < 1000; i++) {
				array.add(i);
			}
			assertEquals(1000, array.size());
		}

		@Test
		@DisplayName("trimToSize reduces internal array")
		void trimToSize() {
			array.ensureCapacity(1000);
			array.add(1);
			array.add(2);
			array.trimToSize();
			assertEquals(2, array.size());
			assertEquals(1, array.get(1));
			assertEquals(2, array.get(2));
		}

		@Test
		@DisplayName("addAll from another IntArray")
		void addAllIntArray() {
			array.add(1);
			array.add(2);
			IntArray other = new IntArray();
			other.add(3);
			other.add(4);
			array.addAll(other);
			assertEquals(4, array.size());
			assertEquals(3, array.get(3));
			assertEquals(4, array.get(4));
		}

		@Test
		@DisplayName("hasRecord always returns false")
		void hasRecordFalse() {
			array.add(1);
			assertFalse(array.hasRecord());
		}

		@Test
		@DisplayName("isPmt always returns false")
		void isPmtFalse() {
			array.add(1);
			assertFalse(array.isPmt(false));
			assertFalse(array.isPmt(true));
		}

		@Test
		@DisplayName("newInstance returns new IntArray")
		void newInstance() {
			IArray instance = array.newInstance(10);
			assertTrue(instance instanceof IntArray);
			assertEquals(0, instance.size());
		}
	}
}
