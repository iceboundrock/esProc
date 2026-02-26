package com.scudata.array;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DoubleArray} — a 1-based double array with null tracking via boolean[] signs.
 */
@DisplayName("DoubleArray Tests")
public class DoubleArrayTest {

	private DoubleArray array;

	@BeforeEach
	void setUp() {
		array = new DoubleArray();
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
		@DisplayName("Capacity constructor creates empty array")
		void capacityConstructor() {
			DoubleArray a = new DoubleArray(50);
			assertEquals(0, a.size());
			for (int i = 0; i < 50; i++) {
				a.add((double) i);
			}
			assertEquals(50, a.size());
		}

		@Test
		@DisplayName("Direct data constructor with signs")
		void directDataConstructor() {
			double[] datas = {0.0, 1.5, 2.5, 3.5};
			boolean[] signs = {false, false, true, false};
			DoubleArray a = new DoubleArray(datas, signs, 3);
			assertEquals(3, a.size());
			assertEquals(1.5, a.get(1));
			assertNull(a.get(2));
			assertEquals(3.5, a.get(3));
		}

		@Test
		@DisplayName("Direct data constructor without signs")
		void directDataConstructorNoSigns() {
			double[] datas = {0.0, 1.1, 2.2, 3.3};
			DoubleArray a = new DoubleArray(datas, null, 3);
			assertEquals(3, a.size());
			assertEquals(1.1, (Double) a.get(1), 0.001);
		}
	}

	// -------------------------------------------------------------------------
	// add / push / get / set
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Element operations")
	class ElementOps {

		@Test
		@DisplayName("add Double values")
		void addDouble() {
			array.add(1.5);
			array.add(2.5);
			assertEquals(2, array.size());
			assertEquals(1.5, (Double) array.get(1), 0.001);
			assertEquals(2.5, (Double) array.get(2), 0.001);
		}

		@Test
		@DisplayName("add null creates signs array")
		void addNull() {
			array.add(1.5);
			array.add(null);
			array.add(3.5);
			assertNull(array.get(2));
		}

		@Test
		@DisplayName("add incompatible type throws exception")
		void addIncompatible() {
			assertThrows(Exception.class, () -> array.add("text"));
		}

		@Test
		@DisplayName("set modifies element")
		void setElement() {
			array.add(1.0);
			array.set(1, 9.9);
			assertEquals(9.9, (Double) array.get(1), 0.001);
		}

		@Test
		@DisplayName("set to null and back")
		void setNullAndBack() {
			array.add(1.0);
			array.set(1, null);
			assertNull(array.get(1));
			array.set(1, 2.0);
			assertEquals(2.0, (Double) array.get(1), 0.001);
		}

		@Test
		@DisplayName("getInt returns truncated int")
		void getIntValue() {
			array.add(42.9);
			assertEquals(42, array.getInt(1));
		}

		@Test
		@DisplayName("getLong returns truncated long")
		void getLongValue() {
			array.add(42.9);
			assertEquals(42L, array.getLong(1));
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
			array.add(1.0);
			array.add(null);
			array.add(3.0);
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("count with no nulls equals size")
		void countNoNulls() {
			array.add(1.0);
			array.add(2.0);
			assertEquals(2, array.count());
		}

		@Test
		@DisplayName("clear resets array")
		void clear() {
			array.add(1.0);
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
		@DisplayName("isNull check")
		void isNullCheck() {
			array.add(1.0);
			array.add(null);
			assertFalse(array.isNull(1));
			assertTrue(array.isNull(2));
		}

		@Test
		@DisplayName("isTrue for non-null")
		void isTrueCheck() {
			array.add(1.0);
			array.add(null);
			assertTrue(array.isTrue(1));
			assertFalse(array.isTrue(2));
		}

		@Test
		@DisplayName("isFalse for null")
		void isFalseCheck() {
			array.add(1.0);
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
		@DisplayName("contains finds existing value")
		void containsExisting() {
			array.add(1.5);
			array.add(2.5);
			array.add(3.5);
			assertTrue(array.contains(2.5));
			assertFalse(array.contains(9.9));
		}

		@Test
		@DisplayName("contains null")
		void containsNull() {
			array.add(1.0);
			array.add(null);
			assertTrue(array.contains(null));
		}

		@Test
		@DisplayName("contains null returns false when no signs")
		void containsNullNoSigns() {
			array.add(1.0);
			assertFalse(array.contains(null));
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
			array.add(1.0);
			array.add(3.0);
			array.insert(2, 2.0);
			assertEquals(3, array.size());
			assertEquals(2.0, (Double) array.get(2), 0.001);
		}

		@Test
		@DisplayName("remove shifts elements left")
		void removeElement() {
			array.add(1.0);
			array.add(2.0);
			array.add(3.0);
			array.remove(2);
			assertEquals(2, array.size());
			assertEquals(3.0, (Double) array.get(2), 0.001);
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
			array.add(3.0);
			array.add(1.0);
			array.add(2.0);
			array.sort();
			assertEquals(1.0, (Double) array.get(1), 0.001);
			assertEquals(2.0, (Double) array.get(2), 0.001);
			assertEquals(3.0, (Double) array.get(3), 0.001);
		}

		@Test
		@DisplayName("sort with nulls puts nulls first")
		void sortWithNulls() {
			array.add(3.0);
			array.add(null);
			array.add(1.0);
			array.sort();
			assertTrue(array.isNull(1));
			assertEquals(1.0, (Double) array.get(2), 0.001);
			assertEquals(3.0, (Double) array.get(3), 0.001);
		}

		@Test
		@DisplayName("rvs returns reversed copy")
		void reverse() {
			array.add(1.0);
			array.add(2.0);
			array.add(3.0);
			IArray reversed = array.rvs();
			assertEquals(3.0, (Double) reversed.get(1), 0.001);
			assertEquals(2.0, (Double) reversed.get(2), 0.001);
			assertEquals(1.0, (Double) reversed.get(3), 0.001);
		}

		@Test
		@DisplayName("rvs with nulls")
		void reverseWithNulls() {
			array.add(1.0);
			array.add(null);
			array.add(3.0);
			IArray reversed = array.rvs();
			assertEquals(3.0, (Double) reversed.get(1), 0.001);
			assertNull(reversed.get(2));
			assertEquals(1.0, (Double) reversed.get(3), 0.001);
		}
	}

	// -------------------------------------------------------------------------
	// Aggregation
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aggregation")
	class Aggregation {

		@Test
		@DisplayName("sum of doubles")
		void sum() {
			array.add(1.5);
			array.add(2.5);
			array.add(3.0);
			assertEquals(7.0, ((Number) array.sum()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("sum skips nulls")
		void sumWithNulls() {
			array.add(1.5);
			array.add(null);
			array.add(3.0);
			assertEquals(4.5, ((Number) array.sum()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("sum of all nulls returns null")
		void sumAllNulls() {
			array.add(null);
			array.add(null);
			assertNull(array.sum());
		}

		@Test
		@DisplayName("average of doubles")
		void average() {
			array.add(10.0);
			array.add(20.0);
			array.add(30.0);
			assertEquals(20.0, ((Number) array.average()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("average skips nulls")
		void averageWithNulls() {
			array.add(10.0);
			array.add(null);
			array.add(30.0);
			assertEquals(20.0, ((Number) array.average()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("average of empty returns null")
		void averageEmpty() {
			assertNull(array.average());
		}

		@Test
		@DisplayName("max")
		void max() {
			array.add(2.0);
			array.add(1.0);
			array.add(3.0);
			assertEquals(3.0, ((Number) array.max()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("max skips nulls")
		void maxWithNulls() {
			array.add(null);
			array.add(1.0);
			array.add(3.0);
			assertEquals(3.0, ((Number) array.max()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("max of empty returns null")
		void maxEmpty() {
			assertNull(array.max());
		}

		@Test
		@DisplayName("min")
		void min() {
			array.add(2.0);
			array.add(1.0);
			array.add(3.0);
			assertEquals(1.0, ((Number) array.min()).doubleValue(), 0.001);
		}

		@Test
		@DisplayName("min skips nulls")
		void minWithNulls() {
			array.add(null);
			array.add(1.0);
			array.add(3.0);
			assertEquals(1.0, ((Number) array.min()).doubleValue(), 0.001);
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
	// Utility
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Utility operations")
	class UtilOps {

		@Test
		@DisplayName("dup creates independent copy")
		void dup() {
			array.add(1.5);
			array.add(null);
			IArray copy = array.dup();
			assertEquals(2, copy.size());
			assertEquals(1.5, (Double) copy.get(1), 0.001);
			assertNull(copy.get(2));

			array.set(1, 9.9);
			assertEquals(1.5, (Double) copy.get(1), 0.001); // independent
		}

		@Test
		@DisplayName("hasRecord returns false")
		void hasRecord() {
			array.add(1.0);
			assertFalse(array.hasRecord());
		}

		@Test
		@DisplayName("isPmt returns false")
		void isPmt() {
			array.add(1.0);
			assertFalse(array.isPmt(false));
		}

		@Test
		@DisplayName("newInstance returns new DoubleArray")
		void newInstance() {
			IArray instance = array.newInstance(10);
			assertTrue(instance instanceof DoubleArray);
			assertEquals(0, instance.size());
		}

		@Test
		@DisplayName("ensureCapacity and trimToSize")
		void ensureCapacityAndTrim() {
			array.ensureCapacity(500);
			array.add(1.0);
			array.add(2.0);
			array.trimToSize();
			assertEquals(2, array.size());
			assertEquals(1.0, (Double) array.get(1), 0.001);
		}
	}
}
