package com.scudata.expression.operator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.expression.Expression;

/**
 * Tests for comparison operators: ==, !=, >, <, >=, <=.
 * Equals uses Variant.isEquals(), Greater/Less use Variant.compare().
 * All return Boolean.TRUE or Boolean.FALSE.
 */
@DisplayName("Comparison Operators Tests")
public class ComparisonOperatorsTest {

    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context();
    }

    // ========== Equals (==) ==========
    @Nested
    @DisplayName("Equals operator (==)")
    class EqualsTests {

        @Test
        @DisplayName("5 == 5 is true")
        void equalIntegers() {
            Object result = new Expression("5==5").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 == 6 is false")
        void unequalIntegers() {
            Object result = new Expression("5==6").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("3.14 == 3.14 is true")
        void equalDoubles() {
            Object result = new Expression("3.14==3.14").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"abc\" == \"abc\" is true")
        void equalStrings() {
            Object result = new Expression("\"abc\"==\"abc\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"abc\" == \"xyz\" is false")
        void unequalStrings() {
            Object result = new Expression("\"abc\"==\"xyz\"").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("null == null is true")
        void nullEqualsNull() {
            ctx.setParamValue("x", null);
            ctx.setParamValue("y", null);
            Object result = new Expression("x==y").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 == null is false")
        void intEqualsNull() {
            ctx.setParamValue("x", null);
            Object result = new Expression("5==x").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("true == true is true")
        void trueEqualsTrue() {
            Object result = new Expression("true==true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("true == false is false")
        void trueEqualsFalse() {
            Object result = new Expression("true==false").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("0 == 0.0 is true (cross-type)")
        void intEqualsDouble() {
            Object result = new Expression("0==0.0").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("-1 == -1 is true")
        void negativeEquals() {
            Object result = new Expression("-1==-1").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"\" == \"\" is true (empty strings)")
        void emptyStringsEqual() {
            Object result = new Expression("\"\"==\"\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }

    // ========== Not Equals (!=) ==========
    @Nested
    @DisplayName("Not Equals operator (!=)")
    class NotEqualsTests {

        @Test
        @DisplayName("5 != 6 is true")
        void unequalIntegers() {
            Object result = new Expression("5!=6").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 != 5 is false")
        void equalIntegers() {
            Object result = new Expression("5!=5").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("\"abc\" != \"xyz\" is true")
        void unequalStrings() {
            Object result = new Expression("\"abc\"!=\"xyz\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"abc\" != \"abc\" is false")
        void equalStrings() {
            Object result = new Expression("\"abc\"!=\"abc\"").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("null != null is false")
        void nullNotEqualsNull() {
            ctx.setParamValue("x", null);
            ctx.setParamValue("y", null);
            Object result = new Expression("x!=y").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("5 != null is true")
        void intNotEqualsNull() {
            ctx.setParamValue("x", null);
            Object result = new Expression("5!=x").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("true != false is true")
        void trueNotEqualsFalse() {
            Object result = new Expression("true!=false").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }

    // ========== Greater Than (>) ==========
    @Nested
    @DisplayName("Greater Than operator (>)")
    class GreaterTests {

        @Test
        @DisplayName("5 > 3 is true")
        void greaterTrue() {
            Object result = new Expression("5>3").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("3 > 5 is false")
        void greaterFalse() {
            Object result = new Expression("3>5").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("5 > 5 is false (not strictly greater)")
        void greaterEqual() {
            Object result = new Expression("5>5").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("3.14 > 2.0 is true")
        void greaterDoubles() {
            Object result = new Expression("3.14>2.0").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"b\" > \"a\" is true (string comparison)")
        void greaterStrings() {
            Object result = new Expression("\"b\">\"a\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"a\" > \"b\" is false")
        void greaterStringsFalse() {
            Object result = new Expression("\"a\">\"b\"").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("0 > -1 is true")
        void greaterThanNegative() {
            Object result = new Expression("0>-1").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 > null is true (null is smallest)")
        void greaterThanNull() {
            ctx.setParamValue("x", null);
            Object result = new Expression("5>x").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("null > 5 is false")
        void nullGreaterThanInt() {
            ctx.setParamValue("x", null);
            Object result = new Expression("x>5").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("100000000000 > 99999999999 is true (long comparison)")
        void greaterLongs() {
            Object result = new Expression("100000000000>99999999999").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }

    // ========== Less Than (<) ==========
    @Nested
    @DisplayName("Less Than operator (<)")
    class LessTests {

        @Test
        @DisplayName("3 < 5 is true")
        void lessTrue() {
            Object result = new Expression("3<5").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 < 3 is false")
        void lessFalse() {
            Object result = new Expression("5<3").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("5 < 5 is false (not strictly less)")
        void lessEqual() {
            Object result = new Expression("5<5").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("2.0 < 3.14 is true")
        void lessDoubles() {
            Object result = new Expression("2.0<3.14").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"a\" < \"b\" is true")
        void lessStrings() {
            Object result = new Expression("\"a\"<\"b\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("-5 < 0 is true")
        void negativeLessThanZero() {
            Object result = new Expression("-5<0").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("null < 5 is true (null is smallest)")
        void nullLessThanInt() {
            ctx.setParamValue("x", null);
            Object result = new Expression("x<5").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 < null is false")
        void intLessThanNull() {
            ctx.setParamValue("x", null);
            Object result = new Expression("5<x").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }
    }

    // ========== Greater Than or Equal (>=) ==========
    @Nested
    @DisplayName("Greater Than or Equal operator (>=)")
    class GreaterOrEqualTests {

        @Test
        @DisplayName("5 >= 3 is true")
        void greaterOrEqualGreater() {
            Object result = new Expression("5>=3").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 >= 5 is true")
        void greaterOrEqualEqual() {
            Object result = new Expression("5>=5").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("3 >= 5 is false")
        void greaterOrEqualLess() {
            Object result = new Expression("3>=5").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("3.14 >= 3.14 is true")
        void greaterOrEqualDoubles() {
            Object result = new Expression("3.14>=3.14").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"b\" >= \"a\" is true")
        void greaterOrEqualStrings() {
            Object result = new Expression("\"b\">=\"a\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"a\" >= \"a\" is true")
        void greaterOrEqualSameStrings() {
            Object result = new Expression("\"a\">=\"a\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("null >= null is true")
        void nullGENull() {
            ctx.setParamValue("x", null);
            ctx.setParamValue("y", null);
            Object result = new Expression("x>=y").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("0 >= -1 is true")
        void zeroGENegative() {
            Object result = new Expression("0>=-1").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }

    // ========== Less Than or Equal (<=) ==========
    @Nested
    @DisplayName("Less Than or Equal operator (<=)")
    class LessOrEqualTests {

        @Test
        @DisplayName("3 <= 5 is true")
        void lessOrEqualLess() {
            Object result = new Expression("3<=5").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 <= 5 is true")
        void lessOrEqualEqual() {
            Object result = new Expression("5<=5").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 <= 3 is false")
        void lessOrEqualGreater() {
            Object result = new Expression("5<=3").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("3.14 <= 3.14 is true")
        void lessOrEqualDoubles() {
            Object result = new Expression("3.14<=3.14").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("\"a\" <= \"b\" is true")
        void lessOrEqualStrings() {
            Object result = new Expression("\"a\"<=\"b\"").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("null <= null is true")
        void nullLENull() {
            ctx.setParamValue("x", null);
            ctx.setParamValue("y", null);
            Object result = new Expression("x<=y").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("null <= 5 is true (null is smallest)")
        void nullLEInt() {
            ctx.setParamValue("x", null);
            Object result = new Expression("x<=5").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("-1 <= 0 is true")
        void negativeLEZero() {
            Object result = new Expression("-1<=0").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }

    // ========== Cross-type comparisons ==========
    @Nested
    @DisplayName("Cross-type comparison tests")
    class CrossTypeTests {

        @Test
        @DisplayName("5 == 5.0 is true (int vs double)")
        void intEqualsDouble() {
            Object result = new Expression("5==5.0").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 > 4.9 is true (int vs double)")
        void intGreaterThanDouble() {
            Object result = new Expression("5>4.9").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 < 5.1 is true (int vs double)")
        void intLessThanDouble() {
            Object result = new Expression("5<5.1").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("100000000000 > 999 is true (long vs int)")
        void longGreaterThanInt() {
            Object result = new Expression("100000000000>999").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("1 != 1.1 is true (int vs double)")
        void intNotEqualsDouble() {
            Object result = new Expression("1!=1.1").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("0 >= 0.0 is true")
        void zeroGEZeroDouble() {
            Object result = new Expression("0>=0.0").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("0 <= 0.0 is true")
        void zeroLEZeroDouble() {
            Object result = new Expression("0<=0.0").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }

    // ========== Chained comparisons (via logical operators) ==========
    @Nested
    @DisplayName("Chained comparison patterns")
    class ChainedTests {

        @Test
        @DisplayName("1 < 5 && 5 < 10 is true (range check)")
        void rangeCheck() {
            Object result = new Expression("1<5&&5<10").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("5 >= 1 && 5 <= 10 is true (inclusive range)")
        void inclusiveRange() {
            Object result = new Expression("5>=1&&5<=10").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("1 == 1 && 2 == 2 is true")
        void multipleEquals() {
            Object result = new Expression("1==1&&2==2").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("1 > 2 || 3 > 2 is true (one true)")
        void orComparison() {
            Object result = new Expression("1>2||3>2").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }
}
