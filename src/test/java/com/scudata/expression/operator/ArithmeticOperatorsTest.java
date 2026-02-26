package com.scudata.expression.operator;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.expression.Expression;

/**
 * Tests for arithmetic operators: +, -, *, /, %.
 * All operators delegate to Variant.* static methods.
 * Tests use Expression.calculate(ctx) for end-to-end evaluation.
 */
@DisplayName("Arithmetic Operators Tests")
public class ArithmeticOperatorsTest {

    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context();
    }

    // ========== Addition (+) ==========
    @Nested
    @DisplayName("Addition operator (+)")
    class AddTests {

        @Test
        @DisplayName("2 + 3 = 5 (int + int)")
        void addIntInt() {
            Object result = new Expression("2+3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5, ((Number) result).intValue());
        }

        @Test
        @DisplayName("2 + 3.0 = 5.0 (int + double)")
        void addIntDouble() {
            Object result = new Expression("2+3.0").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("1.5 + 2.5 = 4.0 (double + double)")
        void addDoubleDouble() {
            Object result = new Expression("1.5+2.5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(4.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("100000000000 + 200000000000 = 300000000000 (long + long)")
        void addLongLong() {
            Object result = new Expression("100000000000+200000000000").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(300000000000L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("2 + 100000000000 = 100000000002 (int + long)")
        void addIntLong() {
            Object result = new Expression("2+100000000000").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(100000000002L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("null + 5 = 5 (null is identity for add)")
        void addNullLeft() {
            ctx.setParamValue("x", null);
            Object result = new Expression("x+5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5, ((Number) result).intValue());
        }

        @Test
        @DisplayName("5 + null = 5 (null is identity for add)")
        void addNullRight() {
            ctx.setParamValue("x", null);
            Object result = new Expression("5+x").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5, ((Number) result).intValue());
        }

        @Test
        @DisplayName("null + null = null")
        void addNullNull() {
            ctx.setParamValue("x", null);
            ctx.setParamValue("y", null);
            Object result = new Expression("x+y").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("0 + 0 = 0")
        void addZeroZero() {
            Object result = new Expression("0+0").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("-5 + 3 = -2 (negative + positive)")
        void addNegativePositive() {
            Object result = new Expression("-5+3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-2, ((Number) result).intValue());
        }

        @Test
        @DisplayName("\"hello\" + \" world\" = \"hello world\" (string concatenation)")
        void addStrings() {
            Object result = new Expression("\"hello\"+\" world\"").calculate(ctx);
            assertEquals("hello world", result);
        }

        @Test
        @DisplayName("\"num: \" + 42 = \"num: 42\" (string + int)")
        void addStringInt() {
            Object result = new Expression("\"num: \"+42").calculate(ctx);
            assertEquals("num: 42", result);
        }

        @Test
        @DisplayName("1 + 2 + 3 = 6 (chained)")
        void addChained() {
            Object result = new Expression("1+2+3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(6, ((Number) result).intValue());
        }

        @Test
        @DisplayName("MAX_INT + 1 overflows to long or wraps")
        void addMaxIntOverflow() {
            ctx.setParamValue("x", Integer.MAX_VALUE);
            Object result = new Expression("x+1").calculate(ctx);
            assertTrue(result instanceof Number);
            // Variant.add handles int overflow by promoting to long
        }
    }

    // ========== Subtraction (-) ==========
    @Nested
    @DisplayName("Subtraction operator (-)")
    class SubtractTests {

        @Test
        @DisplayName("5 - 3 = 2 (int - int)")
        void subtractIntInt() {
            Object result = new Expression("5-3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(2, ((Number) result).intValue());
        }

        @Test
        @DisplayName("3 - 5 = -2")
        void subtractNegativeResult() {
            Object result = new Expression("3-5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-2, ((Number) result).intValue());
        }

        @Test
        @DisplayName("5.5 - 2.5 = 3.0 (double - double)")
        void subtractDoubleDouble() {
            Object result = new Expression("5.5-2.5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("10 - 3.5 = 6.5 (int - double)")
        void subtractIntDouble() {
            Object result = new Expression("10-3.5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(6.5, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("300000000000 - 100000000000 = 200000000000 (long - long)")
        void subtractLongLong() {
            Object result = new Expression("300000000000-100000000000").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(200000000000L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("0 - 0 = 0")
        void subtractZeroZero() {
            Object result = new Expression("0-0").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("-3 - (-5) = 2")
        void subtractNegatives() {
            Object result = new Expression("-3-(-5)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(2, ((Number) result).intValue());
        }

        @Test
        @DisplayName("10 - 3 - 2 = 5 (chained)")
        void subtractChained() {
            Object result = new Expression("10-3-2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5, ((Number) result).intValue());
        }
    }

    // ========== Multiplication (*) ==========
    @Nested
    @DisplayName("Multiplication operator (*)")
    class MultiplyTests {

        @Test
        @DisplayName("3 * 4 = 12 (int * int)")
        void multiplyIntInt() {
            Object result = new Expression("3*4").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(12, ((Number) result).intValue());
        }

        @Test
        @DisplayName("2.5 * 4.0 = 10.0 (double * double)")
        void multiplyDoubleDouble() {
            Object result = new Expression("2.5*4.0").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(10.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("3 * 2.5 = 7.5 (int * double)")
        void multiplyIntDouble() {
            Object result = new Expression("3*2.5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(7.5, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("0 * 999 = 0")
        void multiplyByZero() {
            Object result = new Expression("0*999").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("-3 * 4 = -12")
        void multiplyNegativePositive() {
            Object result = new Expression("-3*4").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-12, ((Number) result).intValue());
        }

        @Test
        @DisplayName("-3 * -4 = 12")
        void multiplyNegativeNegative() {
            Object result = new Expression("-3*(-4)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(12, ((Number) result).intValue());
        }

        @Test
        @DisplayName("1 * x = x (identity)")
        void multiplyByOne() {
            Object result = new Expression("1*42").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("100000 * 100000 = 10000000000 (may promote to long)")
        void multiplyLargeResult() {
            Object result = new Expression("100000*100000").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(10000000000L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("2 * 3 * 4 = 24 (chained)")
        void multiplyChained() {
            Object result = new Expression("2*3*4").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(24, ((Number) result).intValue());
        }

        @Test
        @DisplayName("100000000000 * 2 = 200000000000 (long * int)")
        void multiplyLongInt() {
            Object result = new Expression("100000000000*2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(200000000000L, ((Number) result).longValue());
        }
    }

    // ========== Division (/) ==========
    @Nested
    @DisplayName("Division operator (/)")
    class DivideTests {

        @Test
        @DisplayName("10 / 2 = 5.0 (integer division returns Double)")
        void divideIntInt() {
            Object result = new Expression("10/2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("7 / 2 = 3.5 (non-exact division)")
        void divideNonExact() {
            Object result = new Expression("7/2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3.5, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("10.0 / 3.0 ≈ 3.333...")
        void divideDoubleDouble() {
            Object result = new Expression("10.0/3.0").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(10.0 / 3.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("0 / 5 = 0")
        void divideZeroByNonZero() {
            Object result = new Expression("0/5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("-12 / 4 = -3.0")
        void divideNegativeByPositive() {
            Object result = new Expression("-12/4").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-3.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("-12 / -4 = 3.0")
        void divideNegativeByNegative() {
            Object result = new Expression("-12/(-4)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("100000000000 / 2 = 50000000000 (long / int)")
        void divideLongInt() {
            Object result = new Expression("100000000000/2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(50000000000.0, ((Number) result).doubleValue(), 1.0);
        }

        @Test
        @DisplayName("1 / 3 ≈ 0.333...")
        void divideOneByThree() {
            Object result = new Expression("1/3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1.0 / 3.0, ((Number) result).doubleValue(), 1e-10);
        }
    }

    // ========== Modulo (%) ==========
    @Nested
    @DisplayName("Modulo operator (%)")
    class ModTests {

        @Test
        @DisplayName("10 % 3 = 1")
        void modBasic() {
            Object result = new Expression("10%3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("9 % 3 = 0 (exact division)")
        void modExact() {
            Object result = new Expression("9%3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("7 % 2 = 1")
        void modOdd() {
            Object result = new Expression("7%2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("0 % 5 = 0")
        void modZero() {
            Object result = new Expression("0%5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("5 % 5 = 0")
        void modSameValues() {
            Object result = new Expression("5%5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("3 % 5 = 3 (dividend < divisor)")
        void modSmallDividend() {
            Object result = new Expression("3%5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("10.5 % 3.0 ≈ 1.5 (double mod)")
        void modDoubles() {
            Object result = new Expression("10.5%3.0").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1.5, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("100000000001 % 2 = 1 (long mod)")
        void modLong() {
            Object result = new Expression("100000000001%2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(1, ((Number) result).intValue());
        }
    }

    // ========== Precedence and combinations ==========
    @Nested
    @DisplayName("Operator precedence and combinations")
    class PrecedenceTests {

        @Test
        @DisplayName("2 + 3 * 4 = 14 (multiply before add)")
        void multiplyBeforeAdd() {
            Object result = new Expression("2+3*4").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(14, ((Number) result).intValue());
        }

        @Test
        @DisplayName("(2 + 3) * 4 = 20 (parens override precedence)")
        void parensOverride() {
            Object result = new Expression("(2+3)*4").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(20, ((Number) result).intValue());
        }

        @Test
        @DisplayName("10 - 2 * 3 = 4 (multiply before subtract)")
        void multiplyBeforeSubtract() {
            Object result = new Expression("10-2*3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(4, ((Number) result).intValue());
        }

        @Test
        @DisplayName("10 / 2 + 3 = 8.0 (divide before add)")
        void divideBeforeAdd() {
            Object result = new Expression("10/2+3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(8.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("10 % 3 + 1 = 2 (mod before add)")
        void modBeforeAdd() {
            Object result = new Expression("10%3+1").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(2, ((Number) result).intValue());
        }

        @Test
        @DisplayName("(10 + 5) / 3 = 5.0")
        void complexExpression() {
            Object result = new Expression("(10+5)/3").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("2 * (3 + 4) - 1 = 13")
        void complexExpression2() {
            Object result = new Expression("2*(3+4)-1").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(13, ((Number) result).intValue());
        }

        @Test
        @DisplayName("100 / 10 / 2 = 5.0 (left-to-right)")
        void leftToRightDivision() {
            Object result = new Expression("100/10/2").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("2 + 3 * 4 - 1 = 13")
        void mixedPrecedence() {
            Object result = new Expression("2+3*4-1").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(13, ((Number) result).intValue());
        }

        @Test
        @DisplayName("(2 + 3) * (4 - 1) = 15")
        void doubleParens() {
            Object result = new Expression("(2+3)*(4-1)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(15, ((Number) result).intValue());
        }
    }

    // ========== Unary minus ==========
    @Nested
    @DisplayName("Unary minus operator")
    class UnaryMinusTests {

        @Test
        @DisplayName("-5 evaluates to -5")
        void unaryMinusInt() {
            Object result = new Expression("-5").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-5, ((Number) result).intValue());
        }

        @Test
        @DisplayName("-3.14 evaluates to -3.14")
        void unaryMinusDouble() {
            Object result = new Expression("-3.14").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(-3.14, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("-(-5) = 5 (double negation)")
        void doubleNegation() {
            Object result = new Expression("-(-5)").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(5, ((Number) result).intValue());
        }

        @Test
        @DisplayName("-0 = 0")
        void negateZero() {
            Object result = new Expression("-0").calculate(ctx);
            assertTrue(result instanceof Number);
            assertEquals(0, ((Number) result).intValue());
        }
    }
}
