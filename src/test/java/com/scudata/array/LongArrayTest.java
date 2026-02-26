package com.scudata.array;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LongArray} — a 1-based long integer array with null tracking via boolean[] signs.
 */
@DisplayName("LongArray Tests")
public class LongArrayTest {

	private LongArray array;

	@BeforeEach
	void setUp() {
		array = new LongArray();
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
			LongArray a = new LongArray();
			assertEquals(0, a.size());
		}

		@Test
		@DisplayName("Capacity constructor creates empty array")
		void capacityConstructor() {
			LongArray a = new LongArray(100);
			assertEquals(0, a.size());
			for (int i = 0; i < 100; i++) {
				a.add((long) i);
			}
			assertEquals(100, a.size());
		}

		@Test
		@DisplayName("Direct data constructor with signs")
		void directDataConstructor() {
			long[] datas = {0, 100L, 200L, 300L};
			boolean[] signs = {false, false, true, false};
			LongArray a = new LongArray(datas, signs, 3);
			assertEquals(3, a.size());
			assertEquals(100L, a.get(1));
			assertNull(a.get(2));
			assertEquals(300L, a.get(3));
		}

		@Test
		@DisplayName("Direct data constructor without signs")
		void directDataConstructorNoSigns() {
			long[] datas = {0, 10L, 20L, 30L};
			LongArray a = new LongArray(datas, null, 3);
			assertEquals(3, a.size());
			assertEquals(10L, a.get(1));
			assertEquals(20L, a.get(2));
			assertEquals(30L, a.get(3));
		}
	}

	// -------------------------------------------------------------------------
	// add / push / get / set
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Element operations")
	class ElementOps {

		@Test
		@DisplayName("add Long values")
		void addLong() {
			array.add(100L);
			array.add(200L);
			assertEquals(2, array.size());
			assertEquals(100L, array.get(1));
			assertEquals(200L, array.get(2));
		}

		@Test
		@DisplayName("add Integer values (auto-converts to long)")
		void addInteger() {
			array.add(42); // Integer, accepted by LongArray
			assertEquals(1, array.size());
			assertEquals(42L, ((Number) array.get(1)).longValue());
		}

		@Test
		@DisplayName("add null creates signs array")
		void addNull() {
			array.add(100L);
			array.add(null);
			array.add(300L);
			assertEquals(3, array.size());
			assertNull(array.get(2));
		}

		@Test
		@DisplayName("add incompatible type throws exception")
		void addIncompatibleType() {
			assertThrows(Exception.class, () -> array.add("hello"));
		}

		@Test
		@DisplayName("push(long) adds without capacity check")
		void pushLong() {
			LongArray a = new LongArray(10);
			a.push(42L);
			a.push(99L);
			assertEquals(2, a.size());
			assertEquals(42L, a.get(1));
			assertEquals(99L, a.get(2));
		}

		@Test
		@DisplayName("set modifies existing element")
		void setElement() {
			array.add(100L);
			array.add(200L);
			array.set(1, 999L);
			assertEquals(999L, array.get(1));
		}

		@Test
		@DisplayName("set to null creates signs if needed")
		void setNull() {
			array.add(100L);
			array.set(1, null);
			assertNull(array.get(1));
		}

		@Test
		@DisplayName("set non-null clears sign flag")
		void setNonNullClearsSign() {
			array.add(null);
			array.set(1, 42L);
			assertEquals(42L, array.get(1));
		}

		@Test
		@DisplayName("getInt returns truncated int value")
		void getIntValue() {
			array.add(42L);
			assertEquals(42, array.getInt(1));
		}

		@Test
		@DisplayName("getLong returns raw long value")
		void getLongValue() {
			array.add(Long.MAX_VALUE);
			assertEquals(Long.MAX_VALUE, array.getLong(1));
		}
	}

	// -------------------------------------------------------------------------
	// size / count / clear
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Size and count")
	class SizeCount {

		@Test
		@DisplayName("count excludes null elements")
		void countExcludesNull() {
			array.add(1L);
			array.add(null);
			array.add(3L);
			array.add(null);
			assertEquals(4, array.size());
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("count with no nulls equals size")
		void countNoNulls() {
			array.add(1L);
			array.add(2L);
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("clear resets array")
		void clearResetsArray() {
			array.add(1L);
			array.add(null);
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
		@DisplayName("isNull works correctly")
		void isNullCheck() {
			array.add(1L);
			array.add(null);
			assertFalse(array.isNull(1));
			assertTrue(array.isNull(2));
		}

		@Test
		@DisplayName("isTrue returns true for non-null")
		void isTrueCheck() {
			array.add(42L);
			array.add(null);
			assertTrue(array.isTrue(1));
			assertFalse(array.isTrue(2));
		}

		@Test
		@DisplayName("isFalse returns true for null")
		void isFalseCheck() {
			array.add(42L);
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
		@DisplayName("contains finds existing long value")
		void containsExisting() {
			array.add(10L);
			array.add(20L);
			array.add(30L);
			assertTrue(array.contains(20L));
			assertFalse(array.contains(99L));
		}

		@Test
		@DisplayName("contains(Object) finds Number as long")
		void containsObjectNumber() {
			array.add(10L);
			array.add(20L);
			assertTrue(array.contains((Object) 20L));
			assertTrue(array.contains((Object) 20)); // Integer can match
		}

		@Test
		@DisplayName("contains null when signs present")
		void containsNull() {
			array.add(10L);
			array.add(null);
			assertTrue(array.contains(null));
		}

		@Test
		@DisplayName("contains null returns false when no signs")
		void containsNullNoSigns() {
			array.add(10L);
			assertFalse(array.contains(null));
		}

		@Test
		@DisplayName("binarySearch finds element")
		void binarySearchFinds() {
			for (long i = 1; i <= 10; i++) {
				array.add(i);
			}
			assertEquals(5, array.binarySearch(5L));
		}

		@Test
		@DisplayName("binarySearch returns negative for missing")
		void binarySearchMissing() {
			for (long i = 1; i <= 10; i++) {
				array.add(i);
			}
			assertTrue(array.binarySearch(15L) < 0);
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
			array.add(10L);
			array.add(30L);
			array.insert(2, 20L);
			assertEquals(3, array.size());
			assertEquals(10L, array.get(1));
			assertEquals(20L, array.get(2));
			assertEquals(30L, array.get(3));
		}

		@Test
		@DisplayName("remove shifts elements left")
		void removeElement() {
			array.add(10L);
			array.add(20L);
			array.add(30L);
			array.remove(2);
			assertEquals(2, array.size());
			assertEquals(10L, array.get(1));
			assertEquals(30L, array.get(2));
		}
	}

	// -------------------------------------------------------------------------
	// sort / rvs
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Sort and reverse")
	class SortReverse {

		@Test
		@DisplayName("sort arranges ascending")
		void sortAscending() {
			array.add(30L);
			array.add(10L);
			array.add(20L);
			array.sort();
			assertEquals(10L, array.get(1));
			assertEquals(20L, array.get(2));
			assertEquals(30L, array.get(3));
		}

		@Test
		@DisplayName("sort with nulls puts nulls first")
		void sortWithNulls() {
			array.add(30L);
			array.add(null);
			array.add(10L);
			array.sort();
			assertTrue(array.isNull(1));
			assertEquals(10L, array.get(2));
			assertEquals(30L, array.get(3));
		}

		@Test
		@DisplayName("rvs returns reversed copy")
		void reverse() {
			array.add(10L);
			array.add(20L);
			array.add(30L);
			IArray reversed = array.rvs();
			assertEquals(30L, reversed.get(1));
			assertEquals(20L, reversed.get(2));
			assertEquals(10L, reversed.get(3));
		}

		@Test
		@DisplayName("rvs with nulls reverses correctly")
		void reverseWithNulls() {
			array.add(10L);
			array.add(null);
			array.add(30L);
			IArray reversed = array.rvs();
			assertEquals(30L, reversed.get(1));
			assertNull(reversed.get(2));
			assertEquals(10L, reversed.get(3));
		}
	}

	// -------------------------------------------------------------------------
	// Aggregation: sum, average, min, max
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aggregation")
	class Aggregation {

		@Test
		@DisplayName("sum of all elements")
		void sum() {
			array.add(10L);
			array.add(20L);
			array.add(30L);
			assertEquals(60L, array.sum());
		}

		@Test
		@DisplayName("sum skips nulls")
		void sumWithNulls() {
			array.add(10L);
			array.add(null);
			array.add(30L);
			assertEquals(40L, array.sum());
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
			array.add(10L);
			array.add(20L);
			array.add(30L);
			assertEquals(20.0, ((Number) array.average()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("average skips nulls")
		void averageWithNulls() {
			array.add(10L);
			array.add(null);
			array.add(30L);
			assertEquals(20.0, ((Number) array.average()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("average of empty returns null")
		void averageEmpty() {
			assertNull(array.average());
		}

		@Test
		@DisplayName("max finds maximum value")
		void max() {
			array.add(20L);
			array.add(10L);
			array.add(30L);
			assertEquals(30L, array.max());
		}

		@Test
		@DisplayName("max skips nulls")
		void maxWithNulls() {
			array.add(null);
			array.add(10L);
			array.add(30L);
			assertEquals(30L, array.max());
		}

		@Test
		@DisplayName("max of empty returns null")
		void maxEmpty() {
			assertNull(array.max());
		}

		@Test
		@DisplayName("max of all nulls returns null")
		void maxAllNulls() {
			array.add(null);
			array.add(null);
			assertNull(array.max());
		}

		@Test
		@DisplayName("min finds minimum value")
		void min() {
			array.add(20L);
			array.add(10L);
			array.add(30L);
			assertEquals(10L, array.min());
		}

		@Test
		@DisplayName("min skips nulls")
		void minWithNulls() {
			array.add(null);
			array.add(10L);
			array.add(30L);
			assertEquals(10L, array.min());
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
	// Utility: dup, addAll, hasRecord, isPmt, newInstance
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Utility operations")
	class UtilOps {

		@Test
		@DisplayName("dup creates independent copy")
		void dup() {
			array.add(10L);
			array.add(null);
			array.add(30L);
			IArray copy = array.dup();
			assertEquals(3, copy.size());
			assertEquals(10L, copy.get(1));
			assertNull(copy.get(2));
			assertEquals(30L, copy.get(3));

			array.set(1, 99L);
			assertEquals(10L, copy.get(1)); // independent
		}

		@Test
		@DisplayName("addAll from another LongArray")
		void addAll() {
			array.add(1L);
			LongArray other = new LongArray();
			other.add(2L);
			other.add(3L);
			array.addAll(other);
			assertEquals(3, array.size());
			assertEquals(2L, array.get(2));
			assertEquals(3L, array.get(3));
		}

		@Test
		@DisplayName("hasRecord always returns false")
		void hasRecord() {
			array.add(1L);
			assertFalse(array.hasRecord());
		}

		@Test
		@DisplayName("isPmt always returns false")
		void isPmt() {
			array.add(1L);
			assertFalse(array.isPmt(false));
			assertFalse(array.isPmt(true));
		}

		@Test
		@DisplayName("newInstance returns new LongArray")
		void newInstance() {
			IArray instance = array.newInstance(10);
			assertTrue(instance instanceof LongArray);
			assertEquals(0, instance.size());
		}

		@Test
		@DisplayName("static compare works correctly")
		void staticCompare() {
			assertTrue(LongArray.compare(1L, 2L) < 0);
			assertEquals(0, LongArray.compare(5L, 5L));
			assertTrue(LongArray.compare(10L, 3L) > 0);
		}

		@Test
		@DisplayName("ensureCapacity and trimToSize")
		void ensureCapacityAndTrim() {
			array.ensureCapacity(500);
			array.add(1L);
			array.add(2L);
			array.trimToSize();
			assertEquals(2, array.size());
			assertEquals(1L, array.get(1));
		}
	}
}
