package com.scudata.expression.fn.gather;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;

/**
 * Tests for aggregate/gather functions: sum(), count(), avg(), max(), min().
 * Tests use both sequence-argument and multi-argument forms.
 */
@DisplayName("Gather Functions Tests")
public class GatherFunctionsTest {

    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context();
    }

    /**
     * Helper: create a Sequence from values and set it as a context variable.
     */
    private void setSequenceVar(String name, Object... values) {
        Sequence seq = new Sequence(values.length);
        for (Object v : values) {
            seq.add(v);
        }
        ctx.setParamValue(name, seq);
    }

    // ========== sum() ==========
    @Nested
    @DisplayName("sum() function")
    class SumTests {

        @Test
        @DisplayName("sum of integer sequence [1,2,3] = 6")
        void sumIntegerSequence() {
            setSequenceVar("s", 1, 2, 3);
            Object result = new Expression("sum(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(6, ((Number) result).intValue());
        }

        @Test
        @DisplayName("sum of single element [42] = 42")
        void sumSingleElement() {
            setSequenceVar("s", 42);
            Object result = new Expression("sum(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("sum of empty sequence = null")
        void sumEmptySequence() {
            setSequenceVar("s");
            Object result = new Expression("sum(s)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("sum of doubles [1.5, 2.5, 3.0] = 7.0")
        void sumDoubleSequence() {
            setSequenceVar("s", 1.5, 2.5, 3.0);
            Object result = new Expression("sum(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(7.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("sum of longs [100000000000, 200000000000] = 300000000000")
        void sumLongSequence() {
            setSequenceVar("s", 100000000000L, 200000000000L);
            Object result = new Expression("sum(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(300000000000L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("sum of mixed integers [1, -2, 3, -4, 5] = 3")
        void sumMixedSigns() {
            setSequenceVar("s", 1, -2, 3, -4, 5);
            Object result = new Expression("sum(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("sum with nulls skips them")
        void sumWithNulls() {
            setSequenceVar("s", 1, null, 3, null, 5);
            Object result = new Expression("sum(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(9, ((Number) result).intValue());
        }

        @Test
        @DisplayName("sum multi-arg: sum(1,2,3) = 6")
        void sumMultiArg() {
            Object result = new Expression("sum(1,2,3)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(6, ((Number) result).intValue());
        }

        @Test
        @DisplayName("sum multi-arg: sum(10,20) = 30")
        void sumTwoArgs() {
            Object result = new Expression("sum(10,20)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(30, ((Number) result).intValue());
        }

        @Test
        @DisplayName("sum of scalar returns the scalar itself")
        void sumOfScalar() {
            Object result = new Expression("sum(42)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("sum of [0,0,0] = 0")
        void sumAllZeros() {
            setSequenceVar("s", 0, 0, 0);
            Object result = new Expression("sum(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }
    }

    // ========== count() ==========
    @Nested
    @DisplayName("count() function")
    class CountTests {

        @Test
        @DisplayName("count of [1,2,3] = 3 (all non-null)")
        void countIntegerSequence() {
            setSequenceVar("s", 1, 2, 3);
            Object result = new Expression("count(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count of empty sequence = 0")
        void countEmptySequence() {
            setSequenceVar("s");
            Object result = new Expression("count(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count of single element = 1")
        void countSingleElement() {
            setSequenceVar("s", 42);
            Object result = new Expression("count(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count skips nulls: [1, null, 3, null] = 2")
        void countWithNulls() {
            setSequenceVar("s", 1, null, 3, null);
            Object result = new Expression("count(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(2, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count of all nulls = 0")
        void countAllNulls() {
            setSequenceVar("s", null, null, null);
            Object result = new Expression("count(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count of [true, false, null] - counts truthy values")
        void countBooleans() {
            // count() counts truthy (true) values, not all non-null values
            setSequenceVar("s", true, false, null);
            Object result = new Expression("count(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            // Only true is truthy; false and null are not counted
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count of scalar true = 1")
        void countOfTrueScalar() {
            Object result = new Expression("count(true)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count of scalar null = 0")
        void countOfNullScalar() {
            Object result = new Expression("count(null)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count of strings [\"a\", \"b\", \"c\"] = 3")
        void countStrings() {
            setSequenceVar("s", "a", "b", "c");
            Object result = new Expression("count(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("count multi-arg: count(1,2,3) = 3")
        void countMultiArg() {
            Object result = new Expression("count(1,2,3)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3, ((Number) result).intValue());
        }
    }

    // ========== avg() ==========
    @Nested
    @DisplayName("avg() function")
    class AvgTests {

        @Test
        @DisplayName("avg of [1,2,3] = 2.0")
        void avgIntegerSequence() {
            setSequenceVar("s", 1, 2, 3);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(2.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg of single element [42] = 42")
        void avgSingleElement() {
            setSequenceVar("s", 42);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg of [10, 20, 30] = 20.0")
        void avgEvenDistribution() {
            setSequenceVar("s", 10, 20, 30);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(20.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg of doubles [1.5, 2.5] = 2.0")
        void avgDoubles() {
            setSequenceVar("s", 1.5, 2.5);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(2.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg with nulls [1, null, 5] skips nulls: avg = 3.0")
        void avgWithNulls() {
            setSequenceVar("s", 1, null, 5);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg of empty sequence = null")
        void avgEmptySequence() {
            setSequenceVar("s");
            Object result = new Expression("avg(s)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("avg of [0, 0, 0] = 0")
        void avgAllZeros() {
            setSequenceVar("s", 0, 0, 0);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg of negative numbers [-10, -20] = -15.0")
        void avgNegatives() {
            setSequenceVar("s", -10, -20);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-15.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg of scalar returns the scalar itself")
        void avgOfScalar() {
            Object result = new Expression("avg(42)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("avg multi-arg: avg(10,20,30) = 20.0")
        void avgMultiArg() {
            Object result = new Expression("avg(10,20,30)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(20.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("avg of [1,2,3,4,5] = 3.0")
        void avgFiveElements() {
            setSequenceVar("s", 1, 2, 3, 4, 5);
            Object result = new Expression("avg(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3.0, ((Number) result).doubleValue(), 1e-10);
        }
    }

    // ========== max() ==========
    @Nested
    @DisplayName("max() function")
    class MaxTests {

        @Test
        @DisplayName("max of [1,2,3] = 3")
        void maxIntegerSequence() {
            setSequenceVar("s", 1, 2, 3);
            Object result = new Expression("max(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max of single element [42] = 42")
        void maxSingleElement() {
            setSequenceVar("s", 42);
            Object result = new Expression("max(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max of [5, 1, 9, 3, 7] = 9")
        void maxUnsorted() {
            setSequenceVar("s", 5, 1, 9, 3, 7);
            Object result = new Expression("max(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(9, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max of [-3, -1, -5] = -1")
        void maxNegatives() {
            setSequenceVar("s", -3, -1, -5);
            Object result = new Expression("max(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max of doubles [1.1, 2.2, 3.3] = 3.3")
        void maxDoubles() {
            setSequenceVar("s", 1.1, 2.2, 3.3);
            Object result = new Expression("max(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3.3, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("max with nulls [1, null, 5] = 5")
        void maxWithNulls() {
            setSequenceVar("s", 1, null, 5);
            Object result = new Expression("max(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max of empty sequence = null")
        void maxEmptySequence() {
            setSequenceVar("s");
            Object result = new Expression("max(s)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("max of strings [\"a\", \"c\", \"b\"] = \"c\"")
        void maxStrings() {
            setSequenceVar("s", "a", "c", "b");
            Object result = new Expression("max(s)").calculate(ctx);
            assertEquals("c", result);
        }

        @Test
        @DisplayName("max of scalar returns the scalar")
        void maxOfScalar() {
            Object result = new Expression("max(42)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max multi-arg: max(3,1,2) = 3")
        void maxMultiArg() {
            Object result = new Expression("max(3,1,2)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("max of [0, 0, 0] = 0")
        void maxAllSame() {
            setSequenceVar("s", 0, 0, 0);
            Object result = new Expression("max(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }
    }

    // ========== min() ==========
    @Nested
    @DisplayName("min() function")
    class MinTests {

        @Test
        @DisplayName("min of [1,2,3] = 1")
        void minIntegerSequence() {
            setSequenceVar("s", 1, 2, 3);
            Object result = new Expression("min(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("min of single element [42] = 42")
        void minSingleElement() {
            setSequenceVar("s", 42);
            Object result = new Expression("min(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("min of [5, 1, 9, 3, 7] = 1")
        void minUnsorted() {
            setSequenceVar("s", 5, 1, 9, 3, 7);
            Object result = new Expression("min(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("min of [-3, -1, -5] = -5")
        void minNegatives() {
            setSequenceVar("s", -3, -1, -5);
            Object result = new Expression("min(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-5, ((Number) result).intValue());
        }

        @Test
        @DisplayName("min of doubles [1.1, 2.2, 0.5] = 0.5")
        void minDoubles() {
            setSequenceVar("s", 1.1, 2.2, 0.5);
            Object result = new Expression("min(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0.5, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("min with nulls [1, null, 5] skips nulls = 1")
        void minWithNulls() {
            setSequenceVar("s", 1, null, 5);
            Object result = new Expression("min(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("min of empty sequence = null")
        void minEmptySequence() {
            setSequenceVar("s");
            Object result = new Expression("min(s)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("min of strings [\"a\", \"c\", \"b\"] = \"a\"")
        void minStrings() {
            setSequenceVar("s", "a", "c", "b");
            Object result = new Expression("min(s)").calculate(ctx);
            assertEquals("a", result);
        }

        @Test
        @DisplayName("min of scalar returns the scalar")
        void minOfScalar() {
            Object result = new Expression("min(42)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("min multi-arg: min(3,1,2) = 1")
        void minMultiArg() {
            Object result = new Expression("min(3,1,2)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("min of [0, 0, 0] = 0")
        void minAllSame() {
            setSequenceVar("s", 0, 0, 0);
            Object result = new Expression("min(s)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }
    }

    // ========== Combined / cross-function tests ==========
    @Nested
    @DisplayName("Combined aggregate tests")
    class CombinedTests {

        @Test
        @DisplayName("sum and count agree: sum([1,1,1]) = count([1,1,1]) * 1")
        void sumEqualsCountTimesOne() {
            setSequenceVar("s", 1, 1, 1);
            Object sum = new Expression("sum(s)").calculate(ctx);
            Object count = new Expression("count(s)").calculate(ctx);
            assertEquals(((Number) sum).intValue(), ((Number) count).intValue());
        }

        @Test
        @DisplayName("avg([10,20,30]) = sum([10,20,30]) / count([10,20,30])")
        void avgEqualsSumDivCount() {
            setSequenceVar("s", 10, 20, 30);
            Object sum = new Expression("sum(s)").calculate(ctx);
            Object count = new Expression("count(s)").calculate(ctx);
            Object avg = new Expression("avg(s)").calculate(ctx);
            double expected = ((Number) sum).doubleValue() / ((Number) count).doubleValue();
            assertEquals(expected, ((Number) avg).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("min <= avg <= max for [1,5,10]")
        void minAvgMaxOrdering() {
            setSequenceVar("s", 1, 5, 10);
            Object min = new Expression("min(s)").calculate(ctx);
            Object avg = new Expression("avg(s)").calculate(ctx);
            Object max = new Expression("max(s)").calculate(ctx);
            assertTrue(((Number) min).doubleValue() <= ((Number) avg).doubleValue());
            assertTrue(((Number) avg).doubleValue() <= ((Number) max).doubleValue());
        }

        @Test
        @DisplayName("max - min gives range: [3, 7, 15] range = 12")
        void maxMinusMinIsRange() {
            setSequenceVar("s", 3, 7, 15);
            Object max = new Expression("max(s)").calculate(ctx);
            Object min = new Expression("min(s)").calculate(ctx);
            int range = ((Number) max).intValue() - ((Number) min).intValue();
            assertEquals(12, range);
        }

        @Test
        @DisplayName("all aggregates return null for empty sequence")
        void allNullForEmpty() {
            setSequenceVar("s");
            assertNull(new Expression("sum(s)").calculate(ctx));
            assertNull(new Expression("avg(s)").calculate(ctx));
            assertNull(new Expression("max(s)").calculate(ctx));
            assertNull(new Expression("min(s)").calculate(ctx));
            assertEquals(0, ((Number) new Expression("count(s)").calculate(ctx)).intValue());
        }
    }
}
