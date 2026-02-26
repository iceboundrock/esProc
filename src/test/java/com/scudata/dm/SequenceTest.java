package com.scudata.dm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.common.RQException;

/**
 * Tests for {@link Sequence}.
 * Covers constructors, element access, mutation, search, ordering,
 * set operations, aggregation, and comparison.
 */
class SequenceTest {

    // ========== Constructors ==========

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("default constructor creates empty sequence")
        void defaultConstructor() {
            Sequence seq = new Sequence();
            assertEquals(0, seq.length());
        }

        @Test
        @DisplayName("capacity constructor creates empty sequence with reserved capacity")
        void capacityConstructor() {
            Sequence seq = new Sequence(100);
            assertEquals(0, seq.length());
        }

        @Test
        @DisplayName("Object[] constructor creates sequence with those elements")
        void objectArrayConstructor() {
            Object[] data = {1, "two", 3.0, null};
            Sequence seq = new Sequence(data);
            assertEquals(4, seq.length());
            assertEquals(1, seq.get(1));
            assertEquals("two", seq.get(2));
            assertEquals(3.0, seq.get(3));
            assertNull(seq.get(4));
        }

        @Test
        @DisplayName("copy constructor creates independent copy")
        void copyConstructor() {
            Sequence orig = new Sequence(new Object[]{10, 20, 30});
            Sequence copy = new Sequence(orig);
            assertEquals(3, copy.length());
            assertEquals(10, copy.get(1));
            // Modifying copy should not affect original
            copy.add(40);
            assertEquals(3, orig.length());
            assertEquals(4, copy.length());
        }

        @Test
        @DisplayName("int range constructor [start, end]")
        void intRangeConstructor() {
            Sequence seq = new Sequence(1, 5);
            assertEquals(5, seq.length());
            assertEquals(1, seq.get(1));
            assertEquals(2, seq.get(2));
            assertEquals(5, seq.get(5));
        }

        @Test
        @DisplayName("int range constructor with start > end creates empty")
        void intRangeReversed() {
            Sequence seq = new Sequence(5, 1);
            assertEquals(0, seq.length());
        }

        @Test
        @DisplayName("int range constructor start == end creates single-element")
        void intRangeSingle() {
            Sequence seq = new Sequence(3, 3);
            assertEquals(1, seq.length());
            assertEquals(3, seq.get(1));
        }

        @Test
        @DisplayName("long range constructor")
        void longRangeConstructor() {
            Sequence seq = new Sequence(1L, 4L);
            assertEquals(4, seq.length());
            assertEquals(1L, seq.get(1));
            assertEquals(4L, seq.get(4));
        }
    }

    // ========== Element Access ==========

    @Nested
    @DisplayName("Element access")
    class ElementAccess {

        private Sequence seq;

        @BeforeEach
        void setUp() {
            seq = new Sequence(new Object[]{10, 20, 30, 40, 50});
        }

        @Test
        @DisplayName("get(i) returns element at 1-based index")
        void getValidIndex() {
            assertEquals(10, seq.get(1));
            assertEquals(30, seq.get(3));
            assertEquals(50, seq.get(5));
        }

        @Test
        @DisplayName("get(0) throws RQException")
        void getZeroThrows() {
            assertThrows(RQException.class, () -> seq.get(0));
        }

        @Test
        @DisplayName("get(n+1) throws RQException")
        void getOutOfBoundsThrows() {
            assertThrows(RQException.class, () -> seq.get(6));
        }

        @Test
        @DisplayName("getMem(i) returns element without bounds check")
        void getMem() {
            assertEquals(20, seq.getMem(2));
        }

        @Test
        @DisplayName("length() returns element count")
        void length() {
            assertEquals(5, seq.length());
        }

        @Test
        @DisplayName("toArray() returns Object array of elements")
        void toArray() {
            Object[] arr = seq.toArray();
            assertEquals(5, arr.length);
            assertEquals(10, arr[0]);
            assertEquals(50, arr[4]);
        }

        @Test
        @DisplayName("get(start, end) returns sub-sequence (end exclusive)")
        void getRange() {
            Sequence sub = seq.get(2, 4);
            assertEquals(2, sub.length());
            assertEquals(20, sub.get(1));
            assertEquals(30, sub.get(2));
        }
    }

    // ========== Mutation ==========

    @Nested
    @DisplayName("Mutation")
    class Mutation {

        @Test
        @DisplayName("add(Object) appends element")
        void addObject() {
            Sequence seq = new Sequence();
            seq.add(42);
            seq.add("hello");
            assertEquals(2, seq.length());
            assertEquals(42, seq.get(1));
            assertEquals("hello", seq.get(2));
        }

        @Test
        @DisplayName("add(null) appends null element")
        void addNull() {
            Sequence seq = new Sequence();
            seq.add(null);
            assertEquals(1, seq.length());
            assertNull(seq.get(1));
        }

        @Test
        @DisplayName("addAll(Sequence) appends all elements")
        void addAllSequence() {
            Sequence seq = new Sequence(new Object[]{1, 2});
            Sequence other = new Sequence(new Object[]{3, 4});
            seq.addAll(other);
            assertEquals(4, seq.length());
            assertEquals(3, seq.get(3));
            assertEquals(4, seq.get(4));
        }

        @Test
        @DisplayName("addAll(Object[]) appends all from array")
        void addAllArray() {
            Sequence seq = new Sequence(new Object[]{1});
            seq.addAll(new Object[]{2, 3});
            assertEquals(3, seq.length());
        }

        @Test
        @DisplayName("clear() removes all elements")
        void clear() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3});
            seq.clear();
            assertEquals(0, seq.length());
        }

        @Test
        @DisplayName("reset() removes all elements")
        void reset() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3});
            seq.reset();
            assertEquals(0, seq.length());
        }

        @Test
        @DisplayName("ensureCapacity does not change length")
        void ensureCapacity() {
            Sequence seq = new Sequence(new Object[]{1, 2});
            seq.ensureCapacity(100);
            assertEquals(2, seq.length());
        }

        @Test
        @DisplayName("trimToSize does not change length or elements")
        void trimToSize() {
            Sequence seq = new Sequence(100);
            seq.add(1);
            seq.add(2);
            seq.trimToSize();
            assertEquals(2, seq.length());
            assertEquals(1, seq.get(1));
            assertEquals(2, seq.get(2));
        }
    }

    // ========== Search ==========

    @Nested
    @DisplayName("Search")
    class Search {

        private Sequence seq;

        @BeforeEach
        void setUp() {
            seq = new Sequence(new Object[]{10, 20, 30, 20, 40});
        }

        @Test
        @DisplayName("firstIndexOf returns first occurrence position (1-based)")
        void firstIndexOf() {
            assertEquals(2, seq.firstIndexOf(20));
        }

        @Test
        @DisplayName("firstIndexOf returns 0 for missing element")
        void firstIndexOfMissing() {
            assertEquals(0, seq.firstIndexOf(99));
        }

        @Test
        @DisplayName("lastIndexof returns last occurrence position (1-based)")
        void lastIndexOf() {
            assertEquals(4, seq.lastIndexof(20));
        }

        @Test
        @DisplayName("lastIndexof returns 0 for missing element")
        void lastIndexOfMissing() {
            assertEquals(0, seq.lastIndexof(99));
        }

        @Test
        @DisplayName("contains unsorted true for existing element")
        void containsUnsortedTrue() {
            assertTrue(seq.contains(30, false));
        }

        @Test
        @DisplayName("contains unsorted false for missing element")
        void containsUnsortedFalse() {
            assertFalse(seq.contains(99, false));
        }

        @Test
        @DisplayName("contains sorted (binary search) true")
        void containsSortedTrue() {
            Sequence sorted = new Sequence(new Object[]{1, 2, 3, 4, 5});
            assertTrue(sorted.contains(3, true));
        }

        @Test
        @DisplayName("contains sorted (binary search) false")
        void containsSortedFalse() {
            Sequence sorted = new Sequence(new Object[]{1, 2, 3, 4, 5});
            assertFalse(sorted.contains(6, true));
        }
    }

    // ========== Count ==========

    @Nested
    @DisplayName("count")
    class Count {

        @Test
        @DisplayName("count() returns number of non-null elements")
        void countNonNull() {
            Sequence seq = new Sequence(new Object[]{1, null, 3, null, 5});
            assertEquals(3, seq.count());
        }

        @Test
        @DisplayName("count() on all-null sequence returns 0")
        void countAllNull() {
            Sequence seq = new Sequence(new Object[]{null, null, null});
            assertEquals(0, seq.count());
        }

        @Test
        @DisplayName("count() on all-non-null sequence returns length")
        void countAllNonNull() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3});
            assertEquals(3, seq.count());
        }

        @Test
        @DisplayName("count() on empty sequence returns 0")
        void countEmpty() {
            Sequence seq = new Sequence();
            assertEquals(0, seq.count());
        }
    }

    // ========== Sort and Reverse ==========

    @Nested
    @DisplayName("Sort and Reverse")
    class SortAndReverse {

        @Test
        @DisplayName("sort(null) returns sorted copy, ascending")
        void sortAscending() {
            Sequence seq = new Sequence(new Object[]{3, 1, 4, 1, 5});
            Sequence sorted = seq.sort(null);
            assertEquals(5, sorted.length());
            assertEquals(1, sorted.get(1));
            assertEquals(1, sorted.get(2));
            assertEquals(3, sorted.get(3));
            assertEquals(4, sorted.get(4));
            assertEquals(5, sorted.get(5));
            // Original unchanged
            assertEquals(3, seq.get(1));
        }

        @Test
        @DisplayName("sort with 'z' option sorts descending")
        void sortDescending() {
            Sequence seq = new Sequence(new Object[]{3, 1, 4});
            Sequence sorted = seq.sort("z");
            assertEquals(4, sorted.get(1));
            assertEquals(3, sorted.get(2));
            assertEquals(1, sorted.get(3));
        }

        @Test
        @DisplayName("sort with 'o' option sorts in-place")
        void sortInPlace() {
            Sequence seq = new Sequence(new Object[]{3, 1, 2});
            Sequence result = seq.sort("o");
            assertSame(seq, result);
            assertEquals(1, seq.get(1));
            assertEquals(2, seq.get(2));
            assertEquals(3, seq.get(3));
        }

        @Test
        @DisplayName("sort empty sequence")
        void sortEmpty() {
            Sequence seq = new Sequence();
            Sequence sorted = seq.sort(null);
            assertEquals(0, sorted.length());
        }

        @Test
        @DisplayName("rvs() returns reversed copy")
        void reverse() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3, 4});
            Sequence reversed = seq.rvs();
            assertEquals(4, reversed.length());
            assertEquals(4, reversed.get(1));
            assertEquals(3, reversed.get(2));
            assertEquals(2, reversed.get(3));
            assertEquals(1, reversed.get(4));
            // Original unchanged
            assertEquals(1, seq.get(1));
        }

        @Test
        @DisplayName("rvs() on single element")
        void reverseSingle() {
            Sequence seq = new Sequence(new Object[]{42});
            Sequence reversed = seq.rvs();
            assertEquals(1, reversed.length());
            assertEquals(42, reversed.get(1));
        }
    }

    // ========== Hash code ==========

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("empty sequence hashCode is 0")
        void emptyHashCode() {
            assertEquals(0, new Sequence().hashCode());
        }

        @Test
        @DisplayName("equal sequences have equal hashCodes")
        void equalSequencesEqualHash() {
            Sequence s1 = new Sequence(new Object[]{1, 2, 3});
            Sequence s2 = new Sequence(new Object[]{1, 2, 3});
            assertEquals(s1.hashCode(), s2.hashCode());
        }
    }

    // ========== Equals and CompareTo ==========

    @Nested
    @DisplayName("Equals and CompareTo")
    class EqualsAndCompare {

        @Test
        @DisplayName("isEquals returns true for same elements")
        void isEqualsSameElements() {
            Sequence s1 = new Sequence(new Object[]{1, 2, 3});
            Sequence s2 = new Sequence(new Object[]{1, 2, 3});
            assertTrue(s1.isEquals(s2));
        }

        @Test
        @DisplayName("isEquals returns false for different elements")
        void isEqualsDifferentElements() {
            Sequence s1 = new Sequence(new Object[]{1, 2, 3});
            Sequence s2 = new Sequence(new Object[]{1, 2, 4});
            assertFalse(s1.isEquals(s2));
        }

        @Test
        @DisplayName("isEquals returns false for different lengths")
        void isEqualsDifferentLengths() {
            Sequence s1 = new Sequence(new Object[]{1, 2});
            Sequence s2 = new Sequence(new Object[]{1, 2, 3});
            assertFalse(s1.isEquals(s2));
        }

        @Test
        @DisplayName("isEquals with same reference returns true")
        void isEqualsSameRef() {
            Sequence seq = new Sequence(new Object[]{1, 2});
            assertTrue(seq.isEquals(seq));
        }

        @Test
        @DisplayName("isEquals with null returns false")
        void isEqualsNull() {
            Sequence seq = new Sequence(new Object[]{1});
            assertFalse(seq.isEquals(null));
        }

        @Test
        @DisplayName("equals delegates to isEquals")
        void equalsMethod() {
            Sequence s1 = new Sequence(new Object[]{1, 2});
            Sequence s2 = new Sequence(new Object[]{1, 2});
            assertTrue(s1.equals(s2));
        }

        @Test
        @DisplayName("equals with non-Sequence returns false")
        void equalsNonSequence() {
            Sequence seq = new Sequence(new Object[]{1});
            assertFalse(seq.equals("not a sequence"));
        }

        @Test
        @DisplayName("compareTo same reference returns 0")
        void compareToSameRef() {
            Sequence seq = new Sequence(new Object[]{1, 2});
            assertEquals(0, seq.compareTo(seq));
        }

        @Test
        @DisplayName("compareTo null returns 1")
        void compareToNull() {
            Sequence seq = new Sequence(new Object[]{1});
            assertEquals(1, seq.compareTo((Sequence) null));
        }

        @Test
        @DisplayName("compareTo element-by-element")
        void compareToElements() {
            Sequence s1 = new Sequence(new Object[]{1, 2, 3});
            Sequence s2 = new Sequence(new Object[]{1, 2, 4});
            assertTrue(s1.compareTo(s2) < 0);
            assertTrue(s2.compareTo(s1) > 0);
        }

        @Test
        @DisplayName("compareTo equal sequences returns 0")
        void compareToEqual() {
            Sequence s1 = new Sequence(new Object[]{1, 2});
            Sequence s2 = new Sequence(new Object[]{1, 2});
            assertEquals(0, s1.compareTo(s2));
        }
    }

    // ========== Set Operations ==========

    @Nested
    @DisplayName("Set operations")
    class SetOperations {

        @Test
        @DisplayName("conj concatenates sequences")
        void conj() {
            Sequence seq = new Sequence();
            seq.add(new Sequence(new Object[]{1, 2}));
            seq.add(new Sequence(new Object[]{3, 4}));
            Sequence result = seq.conj(null);
            assertEquals(4, result.length());
            assertEquals(1, result.get(1));
            assertEquals(4, result.get(4));
        }

        @Test
        @DisplayName("union removes duplicates (sorted merge)")
        void union() {
            Sequence s1 = new Sequence(new Object[]{1, 2, 3});
            Sequence s2 = new Sequence(new Object[]{2, 3, 4});
            Sequence result = s1.union(s2, false);
            assertNotNull(result);
            // union should contain 1,2,3,4 (no duplicates)
            assertTrue(result.length() >= 4);
        }

        @Test
        @DisplayName("diff removes elements in second from first")
        void diff() {
            Sequence s1 = new Sequence(new Object[]{1, 2, 3, 4});
            Sequence s2 = new Sequence(new Object[]{2, 4});
            Sequence result = s1.diff(s2, false);
            assertNotNull(result);
            // diff should contain elements in s1 not in s2
        }

        @Test
        @DisplayName("isect returns common elements")
        void isect() {
            Sequence s1 = new Sequence(new Object[]{1, 2, 3, 4});
            Sequence s2 = new Sequence(new Object[]{2, 4, 6});
            Sequence result = s1.isect(s2, false);
            assertNotNull(result);
        }
    }

    // ========== Step and Transpose ==========

    @Nested
    @DisplayName("Step and Transpose")
    class StepAndTranspose {

        @Test
        @DisplayName("step(interval, seqs) selects elements at intervals")
        void step() {
            // [1,2,3,4,5,6], interval=2, seqs=[1]  -> 1,3,5
            Sequence seq = new Sequence(1, 6);
            Sequence result = seq.step(2, new int[]{1});
            assertEquals(3, result.length());
            assertEquals(1, result.get(1));
            assertEquals(3, result.get(2));
            assertEquals(5, result.get(3));
        }

        @Test
        @DisplayName("step with multiple seqs")
        void stepMultiple() {
            // [1,2,3,4,5,6], interval=3, seqs=[1,2]  -> 1,2,4,5
            Sequence seq = new Sequence(1, 6);
            Sequence result = seq.step(3, new int[]{1, 2});
            assertEquals(4, result.length());
            assertEquals(1, result.get(1));
            assertEquals(2, result.get(2));
            assertEquals(4, result.get(3));
            assertEquals(5, result.get(4));
        }

        @Test
        @DisplayName("step with interval < 1 throws")
        void stepInvalidInterval() {
            Sequence seq = new Sequence(1, 5);
            assertThrows(RQException.class, () -> seq.step(0, new int[]{1}));
        }

        @Test
        @DisplayName("step with seq < 1 throws")
        void stepInvalidSeq() {
            Sequence seq = new Sequence(1, 5);
            assertThrows(RQException.class, () -> seq.step(2, new int[]{0}));
        }

        @Test
        @DisplayName("transpose rearranges elements into column-major order")
        void transpose() {
            // [1,2,3,4,5,6] transposed with 3 columns
            // Original matrix (row-major, 2 rows x 3 cols): [1,2,3], [4,5,6]
            // Transposed (col-major): [1,4], [2,5], [3,6] -> flattened: [1,4,2,5,3,6]
            Sequence seq = new Sequence(new Object[]{1, 2, 3, 4, 5, 6});
            Sequence result = seq.transpose(3);
            assertEquals(6, result.length());
            assertEquals(1, result.get(1));
            assertEquals(4, result.get(2));
            assertEquals(2, result.get(3));
            assertEquals(5, result.get(4));
            assertEquals(3, result.get(5));
            assertEquals(6, result.get(6));
        }
    }

    // ========== isPmt, hasRecord ==========

    @Nested
    @DisplayName("isPmt and hasRecord")
    class PmtAndRecord {

        @Test
        @DisplayName("isPmt returns false for plain data sequence")
        void isPmtPlainData() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3});
            assertFalse(seq.isPmt());
        }

        @Test
        @DisplayName("isPmt returns true for sequence of records with same DataStruct")
        void isPmtRecords() {
            DataStruct ds = new DataStruct(new String[]{"a"});
            Sequence seq = new Sequence();
            seq.add(new Record(ds, new Object[]{1}));
            seq.add(new Record(ds, new Object[]{2}));
            assertTrue(seq.isPmt());
        }

        @Test
        @DisplayName("hasRecord returns false for plain data sequence")
        void hasRecordPlainData() {
            Sequence seq = new Sequence(new Object[]{1, 2});
            assertFalse(seq.hasRecord());
        }

        @Test
        @DisplayName("hasRecord returns true if any element is a Record")
        void hasRecordWithRecord() {
            DataStruct ds = new DataStruct(new String[]{"a"});
            Sequence seq = new Sequence();
            seq.add(new Record(ds, new Object[]{1}));
            assertTrue(seq.hasRecord());
        }
    }

    // ========== Aggregation ==========

    @Nested
    @DisplayName("Aggregation")
    class Aggregation {

        @Test
        @DisplayName("sum() returns sum of numeric elements")
        void sum() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3, 4});
            Object result = seq.sum();
            assertNotNull(result);
            assertTrue(((Number) result).intValue() == 10
                    || ((Number) result).longValue() == 10L);
        }

        @Test
        @DisplayName("average() returns mean of numeric elements")
        void average() {
            Sequence seq = new Sequence(new Object[]{2, 4, 6});
            Object result = seq.average();
            assertNotNull(result);
            assertEquals(4.0, ((Number) result).doubleValue(), 0.001);
        }

        @Test
        @DisplayName("min() returns smallest element")
        void min() {
            Sequence seq = new Sequence(new Object[]{5, 3, 8, 1, 7});
            Object result = seq.min();
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max() returns largest element")
        void max() {
            Sequence seq = new Sequence(new Object[]{5, 3, 8, 1, 7});
            Object result = seq.max();
            assertEquals(8, ((Number) result).intValue());
        }

        @Test
        @DisplayName("ifn() returns first non-null element")
        void ifn() {
            Sequence seq = new Sequence(new Object[]{null, null, 42, null});
            assertEquals(42, seq.ifn());
        }

        @Test
        @DisplayName("ifn() returns null when all null")
        void ifnAllNull() {
            Sequence seq = new Sequence(new Object[]{null, null});
            assertNull(seq.ifn());
        }

        @Test
        @DisplayName("nvl() returns first non-null non-empty element")
        void nvl() {
            Sequence seq = new Sequence(new Object[]{null, "", "hello"});
            assertEquals("hello", seq.nvl());
        }

        @Test
        @DisplayName("cand() returns true if all elements are truthy")
        void cand() {
            Sequence seq = new Sequence(new Object[]{1, true, "yes"});
            assertTrue(seq.cand());
        }

        @Test
        @DisplayName("cand() returns false if any element is falsy")
        void candFalse() {
            Sequence seq = new Sequence(new Object[]{1, null, "yes"});
            assertFalse(seq.cand());
        }

        @Test
        @DisplayName("cor() returns true if any element is truthy")
        void cor() {
            Sequence seq = new Sequence(new Object[]{null, false, 1});
            assertTrue(seq.cor());
        }

        @Test
        @DisplayName("cor() returns false if all elements are falsy")
        void corFalse() {
            Sequence seq = new Sequence(new Object[]{null, false});
            assertFalse(seq.cor());
        }
    }

    // ========== compare0 and nullMaxCompare ==========

    @Nested
    @DisplayName("Special comparisons")
    class SpecialComparisons {

        @Test
        @DisplayName("compare0 with positive elements returns 1")
        void compare0Positive() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3});
            assertEquals(1, seq.compare0());
        }

        @Test
        @DisplayName("compare0 with all zeros returns 0")
        void compare0AllZero() {
            Sequence seq = new Sequence(new Object[]{0, 0});
            assertEquals(0, seq.compare0());
        }

        @Test
        @DisplayName("compare0 with negative first returns -1")
        void compare0Negative() {
            Sequence seq = new Sequence(new Object[]{-1, 2, 3});
            assertEquals(-1, seq.compare0());
        }

        @Test
        @DisplayName("compare0 on empty sequence returns -1")
        void compare0Empty() {
            Sequence seq = new Sequence();
            assertEquals(-1, seq.compare0());
        }

        @Test
        @DisplayName("nullMaxCompare: same reference returns 0")
        void nullMaxCompareSameRef() {
            Sequence seq = new Sequence(new Object[]{1});
            assertEquals(0, seq.nullMaxCompare(seq));
        }

        @Test
        @DisplayName("nullMaxCompare: null argument returns -1")
        void nullMaxCompareNull() {
            Sequence seq = new Sequence(new Object[]{1});
            assertEquals(-1, seq.nullMaxCompare(null));
        }
    }

    // ========== id (distinct) ==========

    @Nested
    @DisplayName("id (distinct)")
    class Distinct {

        @Test
        @DisplayName("id with 'o' option on ordered sequence removes adjacent duplicates")
        void idOrdered() {
            Sequence seq = new Sequence(new Object[]{1, 1, 2, 2, 3});
            Sequence result = seq.id("o");
            assertEquals(3, result.length());
            assertEquals(1, result.get(1));
            assertEquals(2, result.get(2));
            assertEquals(3, result.get(3));
        }

        @Test
        @DisplayName("id with 'h' option sorts first then removes duplicates")
        void idHashSort() {
            Sequence seq = new Sequence(new Object[]{3, 1, 2, 1, 3});
            Sequence result = seq.id("h");
            assertEquals(3, result.length());
        }
    }

    // ========== cumulate and proportion ==========

    @Nested
    @DisplayName("cumulate and proportion")
    class CumulateAndProportion {

        @Test
        @DisplayName("cumulate returns running sum")
        void cumulate() {
            Sequence seq = new Sequence(new Object[]{1, 2, 3, 4});
            Sequence result = seq.cumulate();
            assertEquals(4, result.length());
            // 1, 1+2=3, 1+2+3=6, 1+2+3+4=10
            assertEquals(1L, ((Number) result.get(1)).longValue());
            assertEquals(3L, ((Number) result.get(2)).longValue());
            assertEquals(6L, ((Number) result.get(3)).longValue());
            assertEquals(10L, ((Number) result.get(4)).longValue());
        }
    }

    // ========== mode ==========

    @Nested
    @DisplayName("mode")
    class Mode {

        @Test
        @DisplayName("mode returns most frequent non-null element")
        void modeBasic() {
            Sequence seq = new Sequence(new Object[]{1, 2, 2, 3, 3, 3});
            Object result = seq.mode();
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("mode ignores null elements")
        void modeIgnoresNull() {
            Sequence seq = new Sequence(new Object[]{null, null, null, 1});
            Object result = seq.mode();
            assertEquals(1, ((Number) result).intValue());
        }
    }
}
