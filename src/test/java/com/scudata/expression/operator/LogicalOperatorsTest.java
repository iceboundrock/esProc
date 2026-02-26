package com.scudata.expression.operator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.expression.Expression;

/**
 * Tests for logical operators: && (And), || (Or), ! (Not).
 * And/Or use Variant.isTrue() for evaluation.
 * Not uses Variant.isFalse() and returns Boolean.
 * 
 * Key truthy/falsy rules from Variant:
 * - isTrue(null) = false
 * - isTrue(Boolean.FALSE) = false
 * - isTrue(0) = true (! non-null)
 * - isTrue(anything else) = true
 * - isFalse(null) = true
 * - isFalse(Boolean.FALSE) = true  
 * - isFalse(everything else) = false
 */
@DisplayName("Logical Operators Tests")
public class LogicalOperatorsTest {

    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context();
    }

    // ========== And (&&) ==========
    @Nested
    @DisplayName("And operator (&&)")
    class AndTests {

        @Test
        @DisplayName("true && true = true")
        void trueAndTrue() {
            Object result = new Expression("true&&true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("true && false = false")
        void trueAndFalse() {
            Object result = new Expression("true&&false").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("false && true = false")
        void falseAndTrue() {
            Object result = new Expression("false&&true").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("false && false = false")
        void falseAndFalse() {
            Object result = new Expression("false&&false").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("true && null = false (null is falsy)")
        void trueAndNull() {
            ctx.setParamValue("x", null);
            Object result = new Expression("true&&x").calculate(ctx);
            // null is falsy, so true && false = false
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("null && true = false (null short-circuits)")
        void nullAndTrue() {
            ctx.setParamValue("x", null);
            Object result = new Expression("x&&true").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("1 && 1 = true (non-null numbers are truthy)")
        void oneAndOne() {
            Object result = new Expression("1&&1").calculate(ctx);
            // 1 is truthy (non-null, not Boolean.FALSE)
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("0 && 1 = true (0 is truthy in SPL)")
        void zeroAndOne() {
            // In SPL, 0 is non-null and not Boolean.FALSE, so it's truthy
            Object result = new Expression("0&&1").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("true && true && true = true (chained)")
        void chainedAnd() {
            Object result = new Expression("true&&true&&true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("true && false && true = false (short-circuit)")
        void chainedAndShortCircuit() {
            Object result = new Expression("true&&false&&true").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("(1>0) && (2>1) = true")
        void andWithComparisons() {
            Object result = new Expression("(1>0)&&(2>1)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("(1>0) && (0>1) = false")
        void andWithMixedComparisons() {
            Object result = new Expression("(1>0)&&(0>1)").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }
    }

    // ========== Or (||) ==========
    @Nested
    @DisplayName("Or operator (||)")
    class OrTests {

        @Test
        @DisplayName("true || true = true")
        void trueOrTrue() {
            Object result = new Expression("true||true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("true || false = true")
        void trueOrFalse() {
            Object result = new Expression("true||false").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("false || true = true")
        void falseOrTrue() {
            Object result = new Expression("false||true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("false || false = false")
        void falseOrFalse() {
            Object result = new Expression("false||false").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("true || null = true (short-circuit)")
        void trueOrNull() {
            ctx.setParamValue("x", null);
            Object result = new Expression("true||x").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("null || true = true")
        void nullOrTrue() {
            ctx.setParamValue("x", null);
            Object result = new Expression("x||true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("null || false = false")
        void nullOrFalse() {
            ctx.setParamValue("x", null);
            Object result = new Expression("x||false").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("null || null = false (both falsy)")
        void nullOrNull() {
            ctx.setParamValue("x", null);
            ctx.setParamValue("y", null);
            Object result = new Expression("x||y").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("false || false || true = true (chained)")
        void chainedOr() {
            Object result = new Expression("false||false||true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("(0>1) || (1>0) = true")
        void orWithComparisons() {
            Object result = new Expression("(0>1)||(1>0)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("(0>1) || (0>2) = false")
        void orBothFalse() {
            Object result = new Expression("(0>1)||(0>2)").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }
    }

    // ========== Not (!) ==========
    @Nested
    @DisplayName("Not operator (!)")
    class NotTests {

        @Test
        @DisplayName("!true = false")
        void notTrue() {
            Object result = new Expression("!true").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("!false = true")
        void notFalse() {
            Object result = new Expression("!false").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("!null = true (null is falsy)")
        void notNull() {
            ctx.setParamValue("x", null);
            Object result = new Expression("!x").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("!1 = false (1 is truthy)")
        void notOne() {
            Object result = new Expression("!1").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("!0 = false (0 is truthy in SPL)")
        void notZero() {
            // 0 is not Boolean.FALSE and not null, so isTrue=true, isFalse=false
            Object result = new Expression("!0").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("!!true = true (double negation)")
        void doubleNotTrue() {
            Object result = new Expression("!!true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("!!false = false (double negation)")
        void doubleNotFalse() {
            Object result = new Expression("!!false").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("!(1>2) = true")
        void notComparison() {
            Object result = new Expression("!(1>2)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("!(5==5) = false")
        void notEquality() {
            Object result = new Expression("!(5==5)").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("!\"hello\" = false (non-null string is truthy)")
        void notString() {
            Object result = new Expression("!\"hello\"").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }
    }

    // ========== Combined logical operations ==========
    @Nested
    @DisplayName("Combined logical operations")
    class CombinedTests {

        @Test
        @DisplayName("true && true || false = true (and has higher precedence)")
        void andOrPrecedence() {
            Object result = new Expression("true&&true||false").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("false || true && true = true")
        void orAndPrecedence() {
            Object result = new Expression("false||true&&true").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("false || false && true = false")
        void orFalseAndTrue() {
            // && has higher precedence: false || (false && true) = false || false = false
            Object result = new Expression("false||false&&true").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("(false || false) && true = false")
        void parensOverridePrecedence() {
            Object result = new Expression("(false||false)&&true").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("!(true && false) = true")
        void notOfAnd() {
            Object result = new Expression("!(true&&false)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("!(false || false) = true")
        void notOfOr() {
            Object result = new Expression("!(false||false)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("!false && !false = true (De Morgan)")
        void deMorgansLaw1() {
            Object result = new Expression("!false&&!false").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("!true || !true = false (De Morgan)")
        void deMorgansLaw2() {
            Object result = new Expression("!true||!true").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("(1>0) && !(2<1) = true (combined with comparisons)")
        void combinedWithComparisons() {
            Object result = new Expression("(1>0)&&!(2<1)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("(5==5) || (3!=3) = true")
        void eqOrNeq() {
            Object result = new Expression("(5==5)||(3!=3)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("(5>3) && (3<5) && (5>=5) && (5<=5) = true")
        void allComparisons() {
            Object result = new Expression("(5>3)&&(3<5)&&(5>=5)&&(5<=5)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("Complex: (1+2>2) && (10/2==5) = true")
        void complexMixed() {
            Object result = new Expression("(1+2>2)&&(10/2==5)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }
    }

    // ========== Edge cases with SPL-specific truthy/falsy ==========
    @Nested
    @DisplayName("SPL-specific truthy/falsy edge cases")
    class TruthyFalsyEdgeCases {

        @Test
        @DisplayName("\"\" is truthy (non-null, non-false)")
        void emptyStringTruthy() {
            Object result = new Expression("!\"\"").calculate(ctx);
            // empty string is non-null and not Boolean.FALSE, so truthy
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("\"false\" is truthy (it's a string, not Boolean)")
        void stringFalseTruthy() {
            Object result = new Expression("!\"false\"").calculate(ctx);
            // "false" is a non-null String, not Boolean.FALSE → truthy
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("42 is truthy")
        void numberTruthy() {
            Object result = new Expression("!42").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("-1 is truthy")
        void negativeNumberTruthy() {
            Object result = new Expression("!-1").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("3.14 is truthy")
        void doubleTruthy() {
            Object result = new Expression("!3.14").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }
    }
}
