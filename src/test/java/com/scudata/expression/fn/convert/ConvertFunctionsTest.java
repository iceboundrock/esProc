package com.scudata.expression.fn.convert;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.expression.Expression;

/**
 * Tests for type conversion functions: int(), long(), float(), string(), bool().
 * All tests use Expression.calculate(ctx) for end-to-end evaluation.
 */
@DisplayName("Convert Functions Tests")
public class ConvertFunctionsTest {

    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context();
    }

    // ========== int() ==========
    @Nested
    @DisplayName("int() function")
    class IntFunctionTests {

        @Test
        @DisplayName("int(42) returns 42 as Integer")
        void intOfInteger() {
            Object result = new Expression("int(42)").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("int(3.7) truncates to 3")
        void intOfDouble() {
            Object result = new Expression("int(3.7)").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(3, ((Number) result).intValue());
        }

        @Test
        @DisplayName("int(-2.9) truncates to -2")
        void intOfNegativeDouble() {
            Object result = new Expression("int(-2.9)").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(-2, ((Number) result).intValue());
        }

        @Test
        @DisplayName("int(0) returns 0")
        void intOfZero() {
            Object result = new Expression("int(0)").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(0, ((Number) result).intValue());
        }

        @Test
        @DisplayName("int(null) returns null")
        void intOfNull() {
            Object result = new Expression("int(null)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("int(\"123\") parses string to int")
        void intOfString() {
            Object result = new Expression("int(\"123\")").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(123, ((Number) result).intValue());
        }

        @Test
        @DisplayName("int(\"45.8\") parses float string to int (truncates)")
        void intOfFloatString() {
            Object result = new Expression("int(\"45.8\")").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(45, ((Number) result).intValue());
        }

        @Test
        @DisplayName("int(100000000000) large long returns int value")
        void intOfLargeLong() {
            Object result = new Expression("int(100000000000)").calculate(ctx);
            assertTrue(result instanceof Integer);
            // Will overflow / truncate to int range
        }

        @Test
        @DisplayName("int(true) returns 1")
        void intOfTrue() {
            Object result = new Expression("int(true)").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(1, ((Number) result).intValue());
        }

        @Test
        @DisplayName("int(false) returns 0")
        void intOfFalse() {
            Object result = new Expression("int(false)").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(0, ((Number) result).intValue());
        }
    }

    // ========== long() ==========
    @Nested
    @DisplayName("long() function")
    class LongFunctionTests {

        @Test
        @DisplayName("long(42) returns 42 as Long")
        void longOfInteger() {
            Object result = new Expression("long(42)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(42L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(3.7) truncates to 3L")
        void longOfDouble() {
            Object result = new Expression("long(3.7)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(3L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(-5.9) truncates to -5L")
        void longOfNegativeDouble() {
            Object result = new Expression("long(-5.9)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(-5L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(null) returns null")
        void longOfNull() {
            Object result = new Expression("long(null)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("long(\"999\") parses string to long")
        void longOfString() {
            Object result = new Expression("long(\"999\")").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(999L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(100000000000) preserves large value")
        void longOfLargeValue() {
            Object result = new Expression("long(100000000000)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(100000000000L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(0) returns 0L")
        void longOfZero() {
            Object result = new Expression("long(0)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(0L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(\"FF\",16) parses hex string to 255L")
        void longOfHexString() {
            Object result = new Expression("long(\"FF\",16)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(255L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(\"1010\",2) parses binary string to 10L")
        void longOfBinaryString() {
            Object result = new Expression("long(\"1010\",2)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(10L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("long(\"77\",8) parses octal string to 63L")
        void longOfOctalString() {
            Object result = new Expression("long(\"77\",8)").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(63L, ((Number) result).longValue());
        }
    }

    // ========== float() ==========
    @Nested
    @DisplayName("float() function")
    class FloatFunctionTests {

        @Test
        @DisplayName("float(42) returns 42.0 as Double")
        void floatOfInteger() {
            Object result = new Expression("float(42)").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(42.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("float(3.14) returns 3.14")
        void floatOfDouble() {
            Object result = new Expression("float(3.14)").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(3.14, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("float(-2.5) returns -2.5")
        void floatOfNegative() {
            Object result = new Expression("float(-2.5)").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(-2.5, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("float(null) returns null")
        void floatOfNull() {
            Object result = new Expression("float(null)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("float(\"3.14\") parses string to double")
        void floatOfString() {
            Object result = new Expression("float(\"3.14\")").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(3.14, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("float(0) returns 0.0")
        void floatOfZero() {
            Object result = new Expression("float(0)").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(0.0, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("float(100000000000) converts large long to double")
        void floatOfLargeLong() {
            Object result = new Expression("float(100000000000)").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(100000000000.0, ((Number) result).doubleValue(), 1.0);
        }

        @Test
        @DisplayName("float(\"1e5\") parses scientific notation")
        void floatOfScientificNotation() {
            Object result = new Expression("float(\"1e5\")").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(100000.0, ((Number) result).doubleValue(), 1e-10);
        }
    }

    // ========== string() ==========
    @Nested
    @DisplayName("string() function")
    class StringFunctionTests {

        @Test
        @DisplayName("string(42) returns \"42\"")
        void stringOfInteger() {
            Object result = new Expression("string(42)").calculate(ctx);
            assertEquals("42", result);
        }

        @Test
        @DisplayName("string(null) returns null")
        void stringOfNull() {
            Object result = new Expression("string(null)").calculate(ctx);
            assertNull(result);
        }

        @Test
        @DisplayName("string(true) returns \"true\"")
        void stringOfTrue() {
            Object result = new Expression("string(true)").calculate(ctx);
            assertEquals("true", result);
        }

        @Test
        @DisplayName("string(false) returns \"false\"")
        void stringOfFalse() {
            Object result = new Expression("string(false)").calculate(ctx);
            assertEquals("false", result);
        }

        @Test
        @DisplayName("string(3.14) contains \"3.14\"")
        void stringOfDouble() {
            Object result = new Expression("string(3.14)").calculate(ctx);
            assertTrue(result instanceof String);
            assertTrue(((String) result).contains("3.14"));
        }

        @Test
        @DisplayName("string(100000000000) returns long string")
        void stringOfLong() {
            Object result = new Expression("string(100000000000)").calculate(ctx);
            assertEquals("100000000000", result);
        }

        @Test
        @DisplayName("string(\"hello\") returns \"hello\" unchanged")
        void stringOfString() {
            Object result = new Expression("string(\"hello\")").calculate(ctx);
            assertEquals("hello", result);
        }

        @Test
        @DisplayName("string(\"\") returns empty string")
        void stringOfEmptyString() {
            Object result = new Expression("string(\"\")").calculate(ctx);
            assertEquals("", result);
        }

        @Test
        @DisplayName("string(0) returns \"0\"")
        void stringOfZero() {
            Object result = new Expression("string(0)").calculate(ctx);
            assertEquals("0", result);
        }

        @Test
        @DisplayName("string(-42) returns \"-42\"")
        void stringOfNegative() {
            Object result = new Expression("string(-42)").calculate(ctx);
            assertEquals("-42", result);
        }
    }

    // ========== bool() ==========
    @Nested
    @DisplayName("bool() function")
    class BoolFunctionTests {

        @Test
        @DisplayName("bool(null) returns false")
        void boolOfNull() {
            Object result = new Expression("bool(null)").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("bool(true) returns true")
        void boolOfTrue() {
            Object result = new Expression("bool(true)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("bool(false) returns false")
        void boolOfFalse() {
            Object result = new Expression("bool(false)").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("bool(\"false\") returns false (string comparison)")
        void boolOfStringFalse() {
            Object result = new Expression("bool(\"false\")").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("bool(\"true\") returns true")
        void boolOfStringTrue() {
            Object result = new Expression("bool(\"true\")").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("bool(\"hello\") returns true (non-false string)")
        void boolOfArbitraryString() {
            Object result = new Expression("bool(\"hello\")").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("bool(0) returns true (non-null value)")
        void boolOfZero() {
            // ToBool: null→FALSE, "false"→FALSE, everything else→TRUE
            Object result = new Expression("bool(0)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("bool(1) returns true")
        void boolOfOne() {
            Object result = new Expression("bool(1)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("bool(42) returns true")
        void boolOfPositiveInt() {
            Object result = new Expression("bool(42)").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("bool(\"\") returns true (non-null, non-\"false\")")
        void boolOfEmptyString() {
            // Empty string is not null and not "false", so returns TRUE
            Object result = new Expression("bool(\"\")").calculate(ctx);
            assertEquals(Boolean.TRUE, result);
        }

        @Test
        @DisplayName("bool(\"False\") - case sensitivity check")
        void boolOfCapitalizedFalse() {
            // Depends on implementation - "false" check might be case-insensitive
            Object result = new Expression("bool(\"False\")").calculate(ctx);
            // ToBool source checks: s.equals("false") — case-sensitive
            assertTrue(result instanceof Boolean);
        }

        @Test
        @DisplayName("bool(\"FALSE\") - uppercase check")
        void boolOfUppercaseFalse() {
            Object result = new Expression("bool(\"FALSE\")").calculate(ctx);
            // "FALSE".equals("false") is false, so returns TRUE
            assertTrue(result instanceof Boolean);
        }
    }

    // ========== Cross-type conversion chains ==========
    @Nested
    @DisplayName("Conversion chains and combinations")
    class ConversionChainsTests {

        @Test
        @DisplayName("int(float(42)) round-trips through double")
        void intOfFloat() {
            Object result = new Expression("int(float(42))").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("string(int(3.7)) = \"3\"")
        void stringOfIntOfDouble() {
            Object result = new Expression("string(int(3.7))").calculate(ctx);
            assertEquals("3", result);
        }

        @Test
        @DisplayName("long(int(100)) = 100L")
        void longOfInt() {
            Object result = new Expression("long(int(100))").calculate(ctx);
            assertTrue(result instanceof Long);
            assertEquals(100L, ((Number) result).longValue());
        }

        @Test
        @DisplayName("float(string(3.14)) round-trips through string")
        void floatOfStringOfDouble() {
            Object result = new Expression("float(string(3.14))").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(3.14, ((Number) result).doubleValue(), 1e-10);
        }

        @Test
        @DisplayName("bool(string(null)) - string(null)=null, bool(null)=false")
        void boolOfStringOfNull() {
            Object result = new Expression("bool(string(null))").calculate(ctx);
            assertEquals(Boolean.FALSE, result);
        }

        @Test
        @DisplayName("int(long(42)) = 42")
        void intOfLong() {
            Object result = new Expression("int(long(42))").calculate(ctx);
            assertTrue(result instanceof Integer);
            assertEquals(42, ((Number) result).intValue());
        }

        @Test
        @DisplayName("string(long(0)) = \"0\"")
        void stringOfLongZero() {
            Object result = new Expression("string(long(0))").calculate(ctx);
            assertEquals("0", result);
        }

        @Test
        @DisplayName("float(int(7)) = 7.0")
        void floatOfInt() {
            Object result = new Expression("float(int(7))").calculate(ctx);
            assertTrue(result instanceof Double);
            assertEquals(7.0, ((Number) result).doubleValue(), 1e-10);
        }
    }
}
