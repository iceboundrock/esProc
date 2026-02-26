package com.scudata.dm;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Comparator;

@DisplayName("Tests for ListBase1")
class ListBase1Test {

	private ListBase1 list;

	@BeforeEach
	void setUp() {
		list = new ListBase1();
	}

	// ---- Constructors ----

	@Test
	@DisplayName("Default constructor creates empty list with capacity 10")
	void testDefaultConstructor() {
		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}

	@Test
	@DisplayName("Constructor with positive capacity creates empty list")
	void testCapacityConstructor() {
		ListBase1 l = new ListBase1(50);
		assertEquals(0, l.size());
		assertTrue(l.isEmpty());
		// Should be able to add up to 50 elements without resize
		for (int i = 0; i < 50; i++) {
			l.add(i);
		}
		assertEquals(50, l.size());
	}

	@Test
	@DisplayName("Constructor with zero or negative capacity creates minimal list")
	void testZeroCapacityConstructor() {
		ListBase1 l = new ListBase1(0);
		assertEquals(0, l.size());
		// Should still work — add triggers ensureCapacity
		l.add("a");
		assertEquals(1, l.size());
		assertEquals("a", l.get(1));

		ListBase1 l2 = new ListBase1(-5);
		assertEquals(0, l2.size());
		l2.add("b");
		assertEquals("b", l2.get(1));
	}

	@Test
	@DisplayName("Constructor from Object array copies elements at 1-based positions")
	void testArrayConstructor() {
		Object[] arr = {"x", "y", "z"};
		ListBase1 l = new ListBase1(arr);
		assertEquals(3, l.size());
		assertEquals("x", l.get(1));
		assertEquals("y", l.get(2));
		assertEquals("z", l.get(3));
	}

	@Test
	@DisplayName("Copy constructor creates independent copy")
	void testCopyConstructor() {
		list.add("a");
		list.add("b");
		list.add("c");

		ListBase1 copy = new ListBase1(list);
		assertEquals(3, copy.size());
		assertEquals("a", copy.get(1));
		assertEquals("b", copy.get(2));
		assertEquals("c", copy.get(3));

		// Mutating original does not affect copy
		list.set(1, "modified");
		assertEquals("a", copy.get(1));
	}

	// ---- size / isEmpty ----

	@Test
	@DisplayName("size and isEmpty reflect add/remove operations")
	void testSizeAndIsEmpty() {
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());

		list.add("a");
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());

		list.add("b");
		assertEquals(2, list.size());

		list.remove(1);
		assertEquals(1, list.size());

		list.remove(1);
		assertTrue(list.isEmpty());
	}

	// ---- add / get / set ----

	@Test
	@DisplayName("add appends elements and get retrieves them (1-based)")
	void testAddAndGet() {
		list.add(10);
		list.add(20);
		list.add(30);

		assertEquals(3, list.size());
		assertEquals(10, list.get(1));
		assertEquals(20, list.get(2));
		assertEquals(30, list.get(3));
	}

	@Test
	@DisplayName("set replaces element at 1-based index")
	void testSet() {
		list.add("a");
		list.add("b");
		list.add("c");

		list.set(2, "B");
		assertEquals("B", list.get(2));
		assertEquals("a", list.get(1));
		assertEquals("c", list.get(3));
	}

	@Test
	@DisplayName("add(index, element) inserts at 1-based position and shifts right")
	void testAddAtIndex() {
		list.add("a");
		list.add("c");
		list.add(2, "b"); // insert at position 2

		assertEquals(3, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
	}

	@Test
	@DisplayName("add(index, element) can insert at position 1 (beginning)")
	void testAddAtBeginning() {
		list.add("b");
		list.add("c");
		list.add(1, "a");

		assertEquals(3, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
	}

	// ---- remove ----

	@Test
	@DisplayName("remove(index) removes element and shifts left, returns old value")
	void testRemoveSingleElement() {
		list.add("a");
		list.add("b");
		list.add("c");

		Object removed = list.remove(2);
		assertEquals("b", removed);
		assertEquals(2, list.size());
		assertEquals("a", list.get(1));
		assertEquals("c", list.get(2));
	}

	@Test
	@DisplayName("remove(int[]) batch removes by sorted indices")
	void testBatchRemove() {
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");

		// Remove indices 2 and 4 ("b" and "d")
		list.remove(new int[]{2, 4});
		assertEquals(3, list.size());
		assertEquals("a", list.get(1));
		assertEquals("c", list.get(2));
		assertEquals("e", list.get(3));
	}

	@Test
	@DisplayName("remove(int[]) with consecutive indices")
	void testBatchRemoveConsecutive() {
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");

		list.remove(new int[]{2, 3});
		assertEquals(2, list.size());
		assertEquals("a", list.get(1));
		assertEquals("d", list.get(2));
	}

	@Test
	@DisplayName("remove(int[]) removing last element")
	void testBatchRemoveLastElement() {
		list.add("a");
		list.add("b");
		list.add("c");

		list.remove(new int[]{3});
		assertEquals(2, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
	}

	// ---- clear ----

	@Test
	@DisplayName("clear removes all elements and sets size to 0")
	void testClear() {
		list.add("a");
		list.add("b");
		list.add("c");
		list.clear();

		assertEquals(0, list.size());
		assertTrue(list.isEmpty());
	}

	// ---- contains / objectContains ----

	@Test
	@DisplayName("contains uses Variant.isEquals for comparison")
	void testContains() {
		list.add(1);
		list.add(2);
		list.add(3);

		assertTrue(list.contains(2));
		assertFalse(list.contains(99));
		assertTrue(list.contains(1));
		assertTrue(list.contains(3));
	}

	@Test
	@DisplayName("contains with Comparator")
	void testContainsWithComparator() {
		list.add("apple");
		list.add("banana");

		Comparator<Object> cmp = (a, b) -> ((String) a).compareToIgnoreCase((String) b);
		assertTrue(list.contains("APPLE", cmp));
		assertTrue(list.contains("BANANA", cmp));
		assertFalse(list.contains("cherry", cmp));
	}

	@Test
	@DisplayName("objectContains uses reference equality (==)")
	void testObjectContains() {
		String s1 = new String("hello");
		String s2 = new String("hello");

		list.add(s1);
		assertTrue(list.objectContains(s1));
		assertFalse(list.objectContains(s2)); // different reference
	}

	@Test
	@DisplayName("contains returns false on empty list")
	void testContainsEmpty() {
		assertFalse(list.contains("anything"));
	}

	// ---- firstIndexOf / lastIndexOf / indexOf ----

	@Test
	@DisplayName("firstIndexOf returns 1-based index of first occurrence")
	void testFirstIndexOf() {
		list.add("a");
		list.add("b");
		list.add("a");

		assertEquals(1, list.firstIndexOf("a"));
		assertEquals(2, list.firstIndexOf("b"));
		assertEquals(-1, list.firstIndexOf("z"));
	}

	@Test
	@DisplayName("lastIndexOf returns 1-based index of last occurrence")
	void testLastIndexOf() {
		list.add("a");
		list.add("b");
		list.add("a");

		assertEquals(3, list.lastIndexOf("a"));
		assertEquals(2, list.lastIndexOf("b"));
		assertEquals(-1, list.lastIndexOf("z"));
	}

	@Test
	@DisplayName("indexOf(elem, start, end) searches within 1-based range")
	void testIndexOfRange() {
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("b");

		assertEquals(2, list.indexOf("b", 1, 4));
		assertEquals(4, list.indexOf("b", 3, 4));
		assertEquals(-1, list.indexOf("b", 3, 3));
		assertEquals(-1, list.indexOf("z", 1, 4));
	}

	@Test
	@DisplayName("firstIndexOf with isSorted=true uses binary search")
	void testFirstIndexOfSorted() {
		list.add(1);
		list.add(2);
		list.add(2);
		list.add(3);

		assertEquals(2, list.firstIndexOf(2, true));
		assertEquals(1, list.firstIndexOf(1, true));
		assertEquals(4, list.firstIndexOf(3, true));
		assertEquals(-1, list.firstIndexOf(99, true));
	}

	@Test
	@DisplayName("firstIndexOf with isSorted=false behaves like firstIndexOf")
	void testFirstIndexOfUnsorted() {
		list.add("c");
		list.add("a");
		list.add("a");

		assertEquals(2, list.firstIndexOf("a", false));
		assertEquals(-1, list.firstIndexOf("z", false));
	}

	@Test
	@DisplayName("lastIndexOf with comparator and sorted list")
	void testLastIndexOfSortedComparator() {
		list.add(1);
		list.add(2);
		list.add(2);
		list.add(3);

		Comparator<Object> cmp = (a, b) -> Integer.compare((Integer) a, (Integer) b);
		assertEquals(3, list.lastIndexOf(2, cmp, true));
		assertEquals(1, list.lastIndexOf(1, cmp, true));
		assertEquals(-1, list.lastIndexOf(99, cmp, true));
	}

	@Test
	@DisplayName("lastIndexOf with comparator and unsorted list")
	void testLastIndexOfUnsortedComparator() {
		list.add(3);
		list.add(1);
		list.add(1);
		list.add(2);

		Comparator<Object> cmp = (a, b) -> Integer.compare((Integer) a, (Integer) b);
		assertEquals(3, list.lastIndexOf(1, cmp, false));
		assertEquals(-1, list.lastIndexOf(99, cmp, false));
	}

	// ---- binarySearch ----

	@Test
	@DisplayName("binarySearch returns 1-based index when element found")
	void testBinarySearchFound() {
		list.add(10);
		list.add(20);
		list.add(30);

		assertEquals(1, list.binarySearch(10));
		assertEquals(2, list.binarySearch(20));
		assertEquals(3, list.binarySearch(30));
	}

	@Test
	@DisplayName("binarySearch returns negative insertion point when not found")
	void testBinarySearchNotFound() {
		list.add(10);
		list.add(20);
		list.add(30);

		// 5 < 10 → insertion at 1 → returns -1
		assertTrue(list.binarySearch(5) < 0);
		// 15 between 10 and 20 → insertion at 2 → returns -2
		assertEquals(-2, list.binarySearch(15));
		// 35 > 30 → insertion at 4 → returns -4
		assertEquals(-4, list.binarySearch(35));
	}

	@Test
	@DisplayName("binarySearch with comparator")
	void testBinarySearchWithComparator() {
		list.add(10);
		list.add(20);
		list.add(30);

		Comparator<Object> cmp = (a, b) -> Integer.compare((Integer) a, (Integer) b);
		assertEquals(2, list.binarySearch(20, cmp));
		assertTrue(list.binarySearch(25, cmp) < 0);
	}

	@Test
	@DisplayName("binarySearch with comparator and explicit range")
	void testBinarySearchWithRange() {
		list.add(10);
		list.add(20);
		list.add(30);
		list.add(40);

		Comparator<Object> cmp = (a, b) -> Integer.compare((Integer) a, (Integer) b);
		assertEquals(3, list.binarySearch(30, 2, 4, cmp));
		assertTrue(list.binarySearch(25, 2, 4, cmp) < 0);
	}

	// ---- toArray ----

	@Test
	@DisplayName("toArray returns 0-based array of elements")
	void testToArray() {
		list.add("a");
		list.add("b");
		list.add("c");

		Object[] arr = list.toArray();
		assertEquals(3, arr.length);
		assertEquals("a", arr[0]);
		assertEquals("b", arr[1]);
		assertEquals("c", arr[2]);
	}

	@Test
	@DisplayName("toArray returns empty array for empty list")
	void testToArrayEmpty() {
		Object[] arr = list.toArray();
		assertEquals(0, arr.length);
	}

	@Test
	@DisplayName("toArray(Object[]) copies elements into provided array")
	void testToArrayWithTarget() {
		list.add("a");
		list.add("b");
		Object[] target = new Object[2];
		Object[] result = list.toArray(target);
		assertSame(target, result);
		assertEquals("a", result[0]);
		assertEquals("b", result[1]);
	}

	// ---- addAll ----

	@Test
	@DisplayName("addAll(Object[]) appends all array elements")
	void testAddAllArray() {
		list.add("a");
		list.addAll(new Object[]{"b", "c", "d"});

		assertEquals(4, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
		assertEquals("d", list.get(4));
	}

	@Test
	@DisplayName("addAll(ListBase1) appends all elements from another list")
	void testAddAllListBase1() {
		list.add("a");

		ListBase1 other = new ListBase1();
		other.add("b");
		other.add("c");
		list.addAll(other);

		assertEquals(3, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
	}

	@Test
	@DisplayName("addAll(int, Object[]) inserts array at 1-based position")
	void testAddAllAtIndex() {
		list.add("a");
		list.add("d");
		list.addAll(2, new Object[]{"b", "c"});

		assertEquals(4, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
		assertEquals("d", list.get(4));
	}

	@Test
	@DisplayName("addAll(int, ListBase1) inserts list at 1-based position")
	void testAddAllListAtIndex() {
		list.add("a");
		list.add("d");

		ListBase1 other = new ListBase1();
		other.add("b");
		other.add("c");
		list.addAll(2, other);

		assertEquals(4, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
		assertEquals("d", list.get(4));
	}

	@Test
	@DisplayName("addAll(ListBase1, count) appends first count elements")
	void testAddAllWithCount() {
		list.add("a");

		ListBase1 other = new ListBase1();
		other.add("b");
		other.add("c");
		other.add("d");
		list.addAll(other, 2); // only first 2

		assertEquals(3, list.size());
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
	}

	// ---- removeRange ----

	@Test
	@DisplayName("removeRange removes inclusive range and shifts left")
	void testRemoveRange() {
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");

		list.removeRange(2, 4); // remove b, c, d
		assertEquals(2, list.size());
		assertEquals("a", list.get(1));
		assertEquals("e", list.get(2));
	}

	@Test
	@DisplayName("removeRange with single element range")
	void testRemoveRangeSingle() {
		list.add("a");
		list.add("b");
		list.add("c");

		list.removeRange(2, 2);
		assertEquals(2, list.size());
		assertEquals("a", list.get(1));
		assertEquals("c", list.get(2));
	}

	// ---- reserve ----

	@Test
	@DisplayName("reserve keeps only elements in [start, end]")
	void testReserve() {
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");

		list.reserve(2, 4); // keep b, c, d
		assertEquals(3, list.size());
		assertEquals("b", list.get(1));
		assertEquals("c", list.get(2));
		assertEquals("d", list.get(3));
	}

	// ---- ensureCapacity / trimToSize ----

	@Test
	@DisplayName("ensureCapacity grows internal array to accommodate elements")
	void testEnsureCapacity() {
		ListBase1 l = new ListBase1(2);
		l.add("a");
		l.add("b");
		l.ensureCapacity(100);
		// Existing elements are preserved
		assertEquals(2, l.size());
		assertEquals("a", l.get(1));
		assertEquals("b", l.get(2));
		// Can now add many more without issue
		for (int i = 0; i < 98; i++) {
			l.add(i);
		}
		assertEquals(100, l.size());
	}

	@Test
	@DisplayName("ensureCapacity does nothing when already sufficient")
	void testEnsureCapacityAlreadySufficient() {
		ListBase1 l = new ListBase1(100);
		l.add("a");
		l.ensureCapacity(50); // already bigger
		assertEquals(1, l.size());
		assertEquals("a", l.get(1));
	}

	@Test
	@DisplayName("trimToSize shrinks internal array to fit exactly")
	void testTrimToSize() {
		ListBase1 l = new ListBase1(100);
		l.add("a");
		l.add("b");
		l.trimToSize();
		// Elements preserved
		assertEquals(2, l.size());
		assertEquals("a", l.get(1));
		assertEquals("b", l.get(2));
	}

	@Test
	@DisplayName("trimToSize on already-trimmed list is a no-op")
	void testTrimToSizeNoOp() {
		ListBase1 l = new ListBase1(new Object[]{"a", "b"});
		l.trimToSize(); // already exact size
		assertEquals(2, l.size());
		assertEquals("a", l.get(1));
		assertEquals("b", l.get(2));
	}

	// ---- sort ----

	@Test
	@DisplayName("sort orders elements using provided comparator")
	void testSort() {
		list.add(3);
		list.add(1);
		list.add(2);

		Comparator<Object> cmp = (a, b) -> Integer.compare((Integer) a, (Integer) b);
		list.sort(cmp);

		assertEquals(1, list.get(1));
		assertEquals(2, list.get(2));
		assertEquals(3, list.get(3));
	}

	// ---- addSection ----

	@Test
	@DisplayName("addSection appends portion of source list from srcIndex to end")
	void testAddSection() {
		list.add("a");

		ListBase1 src = new ListBase1();
		src.add("x");
		src.add("y");
		src.add("z");

		list.addSection(src, 2); // y, z
		assertEquals(3, list.size());
		assertEquals("a", list.get(1));
		assertEquals("y", list.get(2));
		assertEquals("z", list.get(3));
	}

	@Test
	@DisplayName("addSection with start and end range")
	void testAddSectionRange() {
		list.add("a");

		ListBase1 src = new ListBase1();
		src.add("w");
		src.add("x");
		src.add("y");
		src.add("z");

		list.addSection(src, 2, 4); // x, y (srcEnd exclusive)
		assertEquals(3, list.size());
		assertEquals("a", list.get(1));
		assertEquals("x", list.get(2));
		assertEquals("y", list.get(3));
	}

	// ---- addAll(int, ListBase1, count) ----

	@Test
	@DisplayName("addAll(index, ListBase1, count) inserts count elements at position")
	void testAddAllAtIndexWithCount() {
		list.add("a");
		list.add("d");

		ListBase1 other = new ListBase1();
		other.add("b");
		other.add("c");
		other.add("SKIP");

		list.addAll(2, other, 2);
		assertEquals(4, list.size());
		assertEquals("a", list.get(1));
		assertEquals("b", list.get(2));
		assertEquals("c", list.get(3));
		assertEquals("d", list.get(4));
	}

	// ---- null handling ----

	@Test
	@DisplayName("List can store and retrieve null elements")
	void testNullElements() {
		list.add(null);
		list.add("a");
		list.add(null);

		assertEquals(3, list.size());
		assertNull(list.get(1));
		assertEquals("a", list.get(2));
		assertNull(list.get(3));

		assertTrue(list.contains(null));
		assertEquals(1, list.firstIndexOf(null));
		assertEquals(3, list.lastIndexOf(null));
	}
}
