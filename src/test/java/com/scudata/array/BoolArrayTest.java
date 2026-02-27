package com.scudata.array;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BoolArray} — a 1-based boolean array with null tracking via boolean[] signs.
 */
@DisplayName("BoolArray Tests")
public class BoolArrayTest {

	private BoolArray array;

	@BeforeEach
	void setUp() {
		array = new BoolArray();
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
			BoolArray a = new BoolArray(50);
			assertEquals(0, a.size());
		}

		@Test
		@DisplayName("Value-fill constructor with true")
		void valueFillTrue() {
			BoolArray a = new BoolArray(true, 5);
			assertEquals(5, a.size());
			for (int i = 1; i <= 5; i++) {
				assertEquals(Boolean.TRUE, a.get(i));
			}
		}

		@Test
		@DisplayName("Value-fill constructor with false")
		void valueFillFalse() {
			BoolArray a = new BoolArray(false, 5);
			assertEquals(5, a.size());
			for (int i = 1; i <= 5; i++) {
				assertEquals(Boolean.FALSE, a.get(i));
			}
		}

		@Test
		@DisplayName("Direct data constructor without signs")
		void directDataNoSigns() {
			boolean[] datas = {false, true, false, true};
			BoolArray a = new BoolArray(datas, 3);
			assertEquals(3, a.size());
			assertEquals(Boolean.TRUE, a.get(1));
			assertEquals(Boolean.FALSE, a.get(2));
			assertEquals(Boolean.TRUE, a.get(3));
		}

		@Test
		@DisplayName("Direct data constructor with signs")
		void directDataWithSigns() {
			boolean[] datas = {false, true, false, true};
			boolean[] signs = {false, false, true, false};
			BoolArray a = new BoolArray(datas, signs, 3);
			assertEquals(3, a.size());
			assertEquals(Boolean.TRUE, a.get(1));
			assertNull(a.get(2)); // signs[2] = true → null
			assertEquals(Boolean.TRUE, a.get(3));
		}
	}

	// -------------------------------------------------------------------------
	// add / push / get / set
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Element operations")
	class ElementOps {

		@Test
		@DisplayName("add Boolean values")
		void addBoolean() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			assertEquals(2, array.size());
			assertEquals(Boolean.TRUE, array.get(1));
			assertEquals(Boolean.FALSE, array.get(2));
		}

		@Test
		@DisplayName("add null creates signs array")
		void addNull() {
			array.add(Boolean.TRUE);
			array.add(null);
			array.add(Boolean.FALSE);
			assertNull(array.get(2));
		}

		@Test
		@DisplayName("add incompatible type throws exception")
		void addIncompatible() {
			assertThrows(Exception.class, () -> array.add(42));
		}

		@Test
		@DisplayName("push adds without capacity check")
		void pushBoolean() {
			BoolArray a = new BoolArray(10);
			a.push(true);
			a.push(false);
			assertEquals(2, a.size());
			assertEquals(Boolean.TRUE, a.get(1));
			assertEquals(Boolean.FALSE, a.get(2));
		}

		@Test
		@DisplayName("set(int, boolean) modifies element")
		void setBooleanValue() {
			array.add(Boolean.TRUE);
			array.set(1, false);
			assertEquals(Boolean.FALSE, array.get(1));
		}

		@Test
		@DisplayName("set(int, Object) to null and back")
		void setNull() {
			array.add(Boolean.TRUE);
			array.set(1, null);
			assertNull(array.get(1));
			array.set(1, Boolean.FALSE);
			assertEquals(Boolean.FALSE, array.get(1));
		}

		@Test
		@DisplayName("get returns Boolean or null")
		void getTypes() {
			array.add(Boolean.TRUE);
			array.add(null);
			Object v1 = array.get(1);
			assertTrue(v1 instanceof Boolean);
			assertTrue((Boolean) v1);
			assertNull(array.get(2));
		}
	}

	// -------------------------------------------------------------------------
	// size / count / clear
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Size and count")
	class SizeCount {

		@Test
		@DisplayName("count counts only true values")
		void countExcludesNull() {
			array.add(Boolean.TRUE);
			array.add(null);
			array.add(Boolean.FALSE);
			array.add(null);
			// count counts only TRUE values, not all non-null
			assertEquals(1, array.count());
		}

		@Test
		@DisplayName("count with no nulls counts only true values")
		void countNoNulls() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			// count counts only TRUE values
			assertEquals(1, array.count());
		}

		@Test
		@DisplayName("clear resets array")
		void clear() {
			array.add(Boolean.TRUE);
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
			array.add(Boolean.TRUE);
			array.add(null);
			assertFalse(array.isNull(1));
			assertTrue(array.isNull(2));
		}

		@Test
		@DisplayName("isTrue for true and false values")
		void isTrueCheck() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.add(null);
			assertTrue(array.isTrue(1));
			assertFalse(array.isTrue(2));
			assertFalse(array.isTrue(3));
		}

		@Test
		@DisplayName("isFalse for true, false, null")
		void isFalseCheck() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.add(null);
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
		@DisplayName("contains finds Boolean.TRUE")
		void containsTrue() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			assertTrue(array.contains(Boolean.TRUE));
		}

		@Test
		@DisplayName("contains finds Boolean.FALSE")
		void containsFalse() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			assertTrue(array.contains(Boolean.FALSE));
		}

		@Test
		@DisplayName("contains null")
		void containsNull() {
			array.add(Boolean.TRUE);
			array.add(null);
			assertTrue(array.contains(null));
		}

		@Test
		@DisplayName("contains null returns false when no nulls")
		void containsNullNoNulls() {
			array.add(Boolean.TRUE);
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
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.insert(2, Boolean.TRUE);
			assertEquals(3, array.size());
			assertEquals(Boolean.TRUE, array.get(2));
			assertEquals(Boolean.FALSE, array.get(3));
		}

		@Test
		@DisplayName("remove shifts elements left")
		void removeElement() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.add(Boolean.TRUE);
			array.remove(2);
			assertEquals(2, array.size());
			assertEquals(Boolean.TRUE, array.get(2));
		}
	}

	// -------------------------------------------------------------------------
	// sort / rvs
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Sort and reverse")
	class SortReverse {

		@Test
		@DisplayName("sort sorts booleans (false < true)")
		void sortBooleans() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.sort();
			// false values come first
			assertEquals(Boolean.FALSE, array.get(1));
			assertEquals(Boolean.FALSE, array.get(2));
			assertEquals(Boolean.TRUE, array.get(3));
			assertEquals(Boolean.TRUE, array.get(4));
		}

		@Test
		@DisplayName("rvs returns reversed copy")
		void reverse() {
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.add(Boolean.TRUE);
			IArray reversed = array.rvs();
			assertEquals(Boolean.TRUE, reversed.get(1));
			assertEquals(Boolean.FALSE, reversed.get(2));
			assertEquals(Boolean.TRUE, reversed.get(3));
		}

		@Test
		@DisplayName("rvs with nulls")
		void reverseWithNulls() {
			array.add(Boolean.TRUE);
			array.add(null);
			array.add(Boolean.FALSE);
			IArray reversed = array.rvs();
			assertEquals(Boolean.FALSE, reversed.get(1));
			assertNull(reversed.get(2));
			assertEquals(Boolean.TRUE, reversed.get(3));
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
			array.add(Boolean.TRUE);
			array.add(null);
			array.add(Boolean.FALSE);
			IArray copy = array.dup();
			assertEquals(3, copy.size());
			assertEquals(Boolean.TRUE, copy.get(1));
			assertNull(copy.get(2));
			assertEquals(Boolean.FALSE, copy.get(3));

			array.set(1, Boolean.FALSE);
			assertEquals(Boolean.TRUE, copy.get(1)); // independent
		}

		@Test
		@DisplayName("addAll from another BoolArray")
		void addAll() {
			array.add(Boolean.TRUE);
			BoolArray other = new BoolArray();
			other.add(Boolean.FALSE);
			other.add(Boolean.TRUE);
			array.addAll(other);
			assertEquals(3, array.size());
			assertEquals(Boolean.FALSE, array.get(2));
			assertEquals(Boolean.TRUE, array.get(3));
		}

		@Test
		@DisplayName("hasRecord returns false")
		void hasRecord() {
			array.add(Boolean.TRUE);
			assertFalse(array.hasRecord());
		}

		@Test
		@DisplayName("isPmt returns false")
		void isPmt() {
			array.add(Boolean.TRUE);
			assertFalse(array.isPmt(false));
		}

		@Test
		@DisplayName("newInstance returns new BoolArray")
		void newInstance() {
			IArray instance = array.newInstance(10);
			assertTrue(instance instanceof BoolArray);
			assertEquals(0, instance.size());
		}

		@Test
		@DisplayName("ensureCapacity and trimToSize")
		void ensureCapacityAndTrim() {
			array.ensureCapacity(500);
			array.add(Boolean.TRUE);
			array.add(Boolean.FALSE);
			array.trimToSize();
			assertEquals(2, array.size());
			assertEquals(Boolean.TRUE, array.get(1));
		}

		@Test
		@DisplayName("getDatas and getSigns accessors")
		void getAccessors() {
			array.add(Boolean.TRUE);
			array.add(null);
			assertNotNull(array.getDatas());
			assertNotNull(array.getSigns());
			assertTrue(array.getDatas()[1]); // datas[1] = true
			assertTrue(array.getSigns()[2]); // signs[2] = true (null marker)
		}
	}
}
