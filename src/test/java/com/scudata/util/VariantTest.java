package com.scudata.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Variant} — the core polymorphic utility class that handles
 * type comparison, arithmetic, date operations across Integer/Long/Double/
 * BigDecimal/String/Date types.
 */
@DisplayName("Variant")
class VariantTest {

	// ───── isTrue / isFalse ─────

	@Nested
	@DisplayName("isTrue / isFalse")
	class IsTrueIsFalse {

		@Test
		@DisplayName("null is false")
		void nullIsFalse() {
			assertFalse(Variant.isTrue(null));
			assertTrue(Variant.isFalse(null));
		}

		@Test
		@DisplayName("Boolean.FALSE is false")
		void booleanFalseIsFalse() {
			assertFalse(Variant.isTrue(Boolean.FALSE));
			assertTrue(Variant.isFalse(Boolean.FALSE));
		}

		@Test
		@DisplayName("Boolean.TRUE is true")
		void booleanTrueIsTrue() {
			assertTrue(Variant.isTrue(Boolean.TRUE));
			assertFalse(Variant.isFalse(Boolean.TRUE));
		}

		@Test
		@DisplayName("non-null non-boolean is true")
		void nonNullNonBooleanIsTrue() {
			assertTrue(Variant.isTrue(0));
			assertTrue(Variant.isTrue(""));
			assertTrue(Variant.isTrue("hello"));
			assertTrue(Variant.isTrue(42));
			assertTrue(Variant.isTrue(3.14));

			assertFalse(Variant.isFalse(0));
			assertFalse(Variant.isFalse(""));
		}
	}

	// ───── add ─────

	@Nested
	@DisplayName("add")
	class Add {

		@Test
		@DisplayName("add(null, x) returns x")
		void addNullLeft() {
			assertEquals(5, Variant.add(null, 5));
			assertEquals("abc", Variant.add(null, "abc"));
		}

		@Test
		@DisplayName("add(x, null) returns x")
		void addNullRight() {
			assertEquals(5, Variant.add(5, null));
		}

		@Test
		@DisplayName("add(null, null) returns null")
		void addBothNull() {
			assertNull(Variant.add(null, null));
		}

		@Test
		@DisplayName("Integer + Integer -> Long (overflow protection)")
		void addIntInt() {
			Object result = Variant.add(3, 4);
			// addNum with DT_INT falls through to DT_LONG
			assertInstanceOf(Long.class, result);
			assertEquals(7L, result);
		}

		@Test
		@DisplayName("Long + Long -> Long")
		void addLongLong() {
			Object result = Variant.add(100L, 200L);
			assertInstanceOf(Long.class, result);
			assertEquals(300L, result);
		}

		@Test
		@DisplayName("Double + Double -> Double")
		void addDoubleDouble() {
			Object result = Variant.add(1.5, 2.5);
			assertInstanceOf(Double.class, result);
			assertEquals(4.0, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("Integer + Double -> Double (type promotion)")
		void addIntDouble() {
			Object result = Variant.add(2, 3.5);
			assertInstanceOf(Double.class, result);
			assertEquals(5.5, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("BigDecimal + Integer -> BigDecimal")
		void addBigDecimalInt() {
			Object result = Variant.add(new BigDecimal("10.5"), 3);
			assertInstanceOf(BigDecimal.class, result);
			assertEquals(0, new BigDecimal("13.5").compareTo((BigDecimal) result));
		}

		@Test
		@DisplayName("String + String -> concatenated String")
		void addStringString() {
			Object result = Variant.add("hello", " world");
			assertEquals("hello world", result);
		}

		@Test
		@DisplayName("String(numeric) + Number -> numeric addition")
		void addStringNumber() {
			// "10" + 5 -> parseNumber("10") = 10, addNum(10, 5) = 15L
			Object result = Variant.add("10", 5);
			assertInstanceOf(Long.class, result);
			assertEquals(15L, result);
		}

		@Test
		@DisplayName("Number + String(numeric) -> numeric addition")
		void addNumberString() {
			Object result = Variant.add(5, "10");
			assertInstanceOf(Long.class, result);
			assertEquals(15L, result);
		}

		@Test
		@DisplayName("Date + Number -> date shifted by days")
		void addDateNumber() {
			java.sql.Date date = java.sql.Date.valueOf("2025-01-01");
			Object result = Variant.add(date, 10);
			assertInstanceOf(java.sql.Date.class, result);
			java.sql.Date expected = java.sql.Date.valueOf("2025-01-11");
			assertEquals(expected.toString(), result.toString());
		}

		@Test
		@DisplayName("Number + Date -> date shifted by days")
		void addNumberDate() {
			java.sql.Date date = java.sql.Date.valueOf("2025-01-01");
			Object result = Variant.add(10, date);
			assertInstanceOf(java.sql.Date.class, result);
		}
	}

	// ───── addNum ─────

	@Nested
	@DisplayName("addNum")
	class AddNum {

		@Test
		@DisplayName("INT + INT falls through to LONG case")
		void intIntFallsToLong() {
			Number result = Variant.addNum(3, 4);
			assertInstanceOf(Long.class, result);
			assertEquals(7L, result);
		}

		@Test
		@DisplayName("overflow protection: INT_MAX + 1")
		void intOverflow() {
			Number result = Variant.addNum(Integer.MAX_VALUE, 1);
			assertInstanceOf(Long.class, result);
			assertEquals((long) Integer.MAX_VALUE + 1L, result);
		}
	}

	// ───── subtract ─────

	@Nested
	@DisplayName("subtract")
	class Subtract {

		@Test
		@DisplayName("subtract(x, null) returns x")
		void subtractNullRight() {
			assertEquals(5, Variant.subtract(5, null));
		}

		@Test
		@DisplayName("subtract(null, x) returns negate(x)")
		void subtractNullLeft() {
			Object result = Variant.subtract(null, 5);
			assertInstanceOf(Integer.class, result);
			assertEquals(-5, result);
		}

		@Test
		@DisplayName("Integer - Integer -> Integer")
		void subtractIntInt() {
			Object result = Variant.subtract(10, 3);
			assertInstanceOf(Integer.class, result);
			assertEquals(7, result);
		}

		@Test
		@DisplayName("Long - Long -> Long")
		void subtractLongLong() {
			Object result = Variant.subtract(100L, 30L);
			assertInstanceOf(Long.class, result);
			assertEquals(70L, result);
		}

		@Test
		@DisplayName("Double - Double -> Double")
		void subtractDoubleDouble() {
			Object result = Variant.subtract(5.5, 2.5);
			assertInstanceOf(Double.class, result);
			assertEquals(3.0, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("BigDecimal - Integer -> BigDecimal")
		void subtractBigDecimalInt() {
			Object result = Variant.subtract(new BigDecimal("10.5"), 3);
			assertInstanceOf(BigDecimal.class, result);
			assertEquals(0, new BigDecimal("7.5").compareTo((BigDecimal) result));
		}

		@Test
		@DisplayName("Date - Date -> Long interval in days")
		void subtractDateDate() {
			java.sql.Date d1 = java.sql.Date.valueOf("2025-01-01");
			java.sql.Date d2 = java.sql.Date.valueOf("2025-01-11");
			Object result = Variant.subtract(d2, d1);
			assertInstanceOf(Long.class, result);
			assertEquals(10L, result);
		}

		@Test
		@DisplayName("Date - Number -> date shifted backward")
		void subtractDateNumber() {
			java.sql.Date date = java.sql.Date.valueOf("2025-01-11");
			Object result = Variant.subtract(date, 10);
			assertInstanceOf(java.sql.Date.class, result);
			assertEquals("2025-01-01", result.toString());
		}
	}

	// ───── multiply ─────

	@Nested
	@DisplayName("multiply")
	class Multiply {

		@Test
		@DisplayName("Integer * Integer -> Long (overflow protection)")
		void multiplyIntInt() {
			Object result = Variant.multiply(3, 4);
			assertInstanceOf(Long.class, result);
			assertEquals(12L, result);
		}

		@Test
		@DisplayName("Double * Double -> Double")
		void multiplyDoubleDouble() {
			Object result = Variant.multiply(2.5, 4.0);
			assertInstanceOf(Double.class, result);
			assertEquals(10.0, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("BigDecimal * Integer -> BigDecimal")
		void multiplyBigDecimalInt() {
			Object result = Variant.multiply(new BigDecimal("3.5"), 2);
			assertInstanceOf(BigDecimal.class, result);
			assertEquals(0, new BigDecimal("7.0").compareTo((BigDecimal) result));
		}

		@Test
		@DisplayName("null * Number -> null")
		void multiplyNullNumber() {
			assertNull(Variant.multiply(null, 5));
		}

		@Test
		@DisplayName("Number * null -> null")
		void multiplyNumberNull() {
			assertNull(Variant.multiply(5, null));
		}
	}

	// ───── divide ─────

	@Nested
	@DisplayName("divide")
	class Divide {

		@Test
		@DisplayName("Integer / Integer -> Double")
		void divideIntInt() {
			Object result = Variant.divide(10, 3);
			assertInstanceOf(Double.class, result);
			double d = ((Double) result).doubleValue();
			assertEquals(10.0 / 3.0, d, 0.0001);
		}

		@Test
		@DisplayName("Double / Double -> Double")
		void divideDoubleDouble() {
			Object result = Variant.divide(10.0, 4.0);
			assertInstanceOf(Double.class, result);
			assertEquals(2.5, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("BigDecimal / BigDecimal -> BigDecimal with scale")
		void divideBigDecimalBigDecimal() {
			Object result = Variant.divide(new BigDecimal("10"), new BigDecimal("3"));
			assertInstanceOf(BigDecimal.class, result);
			// scale is Divide_Scale = 16
			assertEquals(16, ((BigDecimal) result).scale());
		}

		@Test
		@DisplayName("null / null -> null")
		void divideNullNull() {
			assertNull(Variant.divide(null, null));
		}

		@Test
		@DisplayName("null / Number -> null")
		void divideNullNumber() {
			assertNull(Variant.divide(null, 5));
		}

		@Test
		@DisplayName("Number / null -> null")
		void divideNumberNull() {
			assertNull(Variant.divide(5, null));
		}

		@Test
		@DisplayName("String / null -> String (concat behavior)")
		void divideStringNull() {
			assertEquals("hello", Variant.divide("hello", null));
		}

		@Test
		@DisplayName("null / String -> String")
		void divideNullString() {
			assertEquals("world", Variant.divide(null, "world"));
		}

		@Test
		@DisplayName("String / Object -> concatenation")
		void divideStringObject() {
			Object result = Variant.divide("val=", 42);
			assertEquals("val=42", result);
		}

		@Test
		@DisplayName("division by zero produces Infinity")
		void divideByZero() {
			Object result = Variant.divide(10, 0);
			assertInstanceOf(Double.class, result);
			assertTrue(Double.isInfinite(((Double) result).doubleValue()));
		}
	}

	// ───── mod ─────

	@Nested
	@DisplayName("mod")
	class Mod {

		@Test
		@DisplayName("Integer mod Integer")
		void modIntInt() {
			Object result = Variant.mod((Object) 10, (Object) 3);
			assertInstanceOf(Integer.class, result);
			assertEquals(1, result);
		}

		@Test
		@DisplayName("Long mod Long")
		void modLongLong() {
			Object result = Variant.mod((Object) 10L, (Object) 3L);
			assertInstanceOf(Long.class, result);
			assertEquals(1L, result);
		}

		@Test
		@DisplayName("null mod x -> null")
		void modNullLeft() {
			assertNull(Variant.mod((Object) null, (Object) 3));
		}

		@Test
		@DisplayName("x mod null -> null")
		void modNullRight() {
			assertNull(Variant.mod((Object) 10, (Object) null));
		}
	}

	// ───── negate ─────

	@Nested
	@DisplayName("negate")
	class Negate {

		@Test
		@DisplayName("negate null -> null")
		void negateNull() {
			assertNull(Variant.negate((Object) null));
		}

		@Test
		@DisplayName("negate Integer preserves type")
		void negateInteger() {
			Object result = Variant.negate((Object) 5);
			assertInstanceOf(Integer.class, result);
			assertEquals(-5, result);
		}

		@Test
		@DisplayName("negate Long preserves type")
		void negateLong() {
			Object result = Variant.negate((Object) 100L);
			assertInstanceOf(Long.class, result);
			assertEquals(-100L, result);
		}

		@Test
		@DisplayName("negate Double preserves type")
		void negateDouble() {
			Object result = Variant.negate((Object) 3.14);
			assertInstanceOf(Double.class, result);
			assertEquals(-3.14, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("negate BigDecimal preserves type")
		void negateBigDecimal() {
			Object result = Variant.negate((Object) new BigDecimal("7.5"));
			assertInstanceOf(BigDecimal.class, result);
			assertEquals(0, new BigDecimal("-7.5").compareTo((BigDecimal) result));
		}

		@Test
		@DisplayName("negate non-number throws")
		void negateNonNumber() {
			assertThrows(RuntimeException.class, () -> Variant.negate((Object) "abc"));
		}
	}

	// ───── abs ─────

	@Nested
	@DisplayName("abs")
	class Abs {

		@Test
		@DisplayName("abs null -> null")
		void absNull() {
			assertNull(Variant.abs((Object) null));
		}

		@Test
		@DisplayName("abs negative Integer")
		void absNegInt() {
			Object result = Variant.abs((Object) (-5));
			assertInstanceOf(Integer.class, result);
			assertEquals(5, result);
		}

		@Test
		@DisplayName("abs negative Long")
		void absNegLong() {
			Object result = Variant.abs((Object) (-100L));
			assertInstanceOf(Long.class, result);
			assertEquals(100L, result);
		}

		@Test
		@DisplayName("abs negative Double")
		void absNegDouble() {
			Object result = Variant.abs((Object) (-3.14));
			assertInstanceOf(Double.class, result);
			assertEquals(3.14, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("abs negative BigDecimal")
		void absNegBigDecimal() {
			Object result = Variant.abs((Object) new BigDecimal("-7.5"));
			assertInstanceOf(BigDecimal.class, result);
			assertEquals(0, new BigDecimal("7.5").compareTo((BigDecimal) result));
		}
	}

	// ───── compare ─────

	@Nested
	@DisplayName("compare")
	class Compare {

		@Test
		@DisplayName("same reference -> 0")
		void sameReference() {
			Object o = new Integer(42);
			assertEquals(0, Variant.compare(o, o, true));
		}

		@Test
		@DisplayName("null is smallest in compare")
		void nullSmallest() {
			assertEquals(-1, Variant.compare(null, 5, true));
			assertEquals(1, Variant.compare(5, null, true));
		}

		@Test
		@DisplayName("null vs null -> 0")
		void nullNull() {
			assertEquals(0, Variant.compare(null, null, true));
		}

		@Test
		@DisplayName("Integer comparison")
		void compareIntegers() {
			assertEquals(-1, Variant.compare(1, 2, true));
			assertEquals(0, Variant.compare(5, 5, true));
			assertEquals(1, Variant.compare(10, 3, true));
		}

		@Test
		@DisplayName("Long comparison")
		void compareLongs() {
			assertTrue(Variant.compare(100L, 200L, true) < 0);
			assertEquals(0, Variant.compare(100L, 100L, true));
			assertTrue(Variant.compare(200L, 100L, true) > 0);
		}

		@Test
		@DisplayName("Double comparison")
		void compareDoubles() {
			assertTrue(Variant.compare(1.5, 2.5, true) < 0);
			assertEquals(0, Variant.compare(3.14, 3.14, true));
		}

		@Test
		@DisplayName("cross-type number comparison (Integer vs Long)")
		void crossTypeNumber() {
			assertEquals(0, Variant.compare(5, 5L, true));
			assertTrue(Variant.compare(3, 5L, true) < 0);
		}

		@Test
		@DisplayName("String comparison")
		void compareStrings() {
			assertTrue(Variant.compare("abc", "xyz", true) < 0);
			assertEquals(0, Variant.compare("abc", "abc", true));
			assertTrue(Variant.compare("xyz", "abc", true) > 0);
		}

		@Test
		@DisplayName("Date comparison")
		void compareDates() {
			java.sql.Date d1 = java.sql.Date.valueOf("2025-01-01");
			java.sql.Date d2 = java.sql.Date.valueOf("2025-06-15");
			assertTrue(Variant.compare(d1, d2, true) < 0);
			assertEquals(0, Variant.compare(d1, java.sql.Date.valueOf("2025-01-01"), true));
		}

		@Test
		@DisplayName("Boolean comparison")
		void compareBooleans() {
			assertEquals(0, Variant.compare(true, true, true));
			assertEquals(0, Variant.compare(false, false, true));
			assertTrue(Variant.compare(true, false, true) > 0);
			assertTrue(Variant.compare(false, true, true) < 0);
		}

		@Test
		@DisplayName("BigDecimal comparison")
		void compareBigDecimals() {
			BigDecimal a = new BigDecimal("10.5");
			BigDecimal b = new BigDecimal("20.3");
			assertTrue(Variant.compare(a, b, true) < 0);
			assertEquals(0, Variant.compare(a, new BigDecimal("10.5"), true));
		}

		@Test
		@DisplayName("incompatible types with throwExcept=false returns based on type order")
		void incompatibleNoThrow() {
			// Number type = 1, String type = 2
			int cmp = Variant.compare(42, "abc", false);
			assertTrue(cmp < 0); // 1 < 2
		}

		@Test
		@DisplayName("byte array comparison")
		void compareByteArrays() {
			byte[] a = {1, 2, 3};
			byte[] b = {1, 2, 4};
			assertTrue(Variant.compare(a, b, true) < 0);
			assertEquals(0, Variant.compare(new byte[]{1, 2}, new byte[]{1, 2}, true));
		}
	}

	// ───── compare_0 (null as maximum) ─────

	@Nested
	@DisplayName("compare_0")
	class Compare0 {

		@Test
		@DisplayName("null is largest in compare_0")
		void nullIsLargest() {
			assertEquals(1, Variant.compare_0(null, 5));
			assertEquals(-1, Variant.compare_0(5, null));
		}

		@Test
		@DisplayName("null vs null -> 0")
		void nullVsNull() {
			assertEquals(0, Variant.compare_0(null, null));
		}

		@Test
		@DisplayName("normal number compare_0 works same as compare")
		void normalNumbers() {
			assertTrue(Variant.compare_0(1, 2) < 0);
			assertEquals(0, Variant.compare_0(5, 5));
			assertTrue(Variant.compare_0(10, 3) > 0);
		}
	}

	// ───── isEquals ─────

	@Nested
	@DisplayName("isEquals")
	class IsEquals {

		@Test
		@DisplayName("same reference -> true")
		void sameRef() {
			Object o = "test";
			assertTrue(Variant.isEquals(o, o));
		}

		@Test
		@DisplayName("null vs null -> true (same reference)")
		void nullNull() {
			assertTrue(Variant.isEquals(null, null));
		}

		@Test
		@DisplayName("null vs non-null -> false")
		void nullVsNonNull() {
			assertFalse(Variant.isEquals(null, 5));
			assertFalse(Variant.isEquals(5, null));
		}

		@Test
		@DisplayName("Integer equality")
		void intEquality() {
			assertTrue(Variant.isEquals(5, 5));
			assertFalse(Variant.isEquals(5, 6));
		}

		@Test
		@DisplayName("cross-type number equality (Integer vs Long)")
		void crossTypeEquals() {
			assertTrue(Variant.isEquals(5, 5L));
			assertTrue(Variant.isEquals(5L, 5));
		}

		@Test
		@DisplayName("Integer vs Double equality")
		void intDoubleEquals() {
			assertTrue(Variant.isEquals(5, 5.0));
			assertFalse(Variant.isEquals(5, 5.1));
		}

		@Test
		@DisplayName("BigDecimal equality ignores scale")
		void bigDecimalScaleIgnored() {
			// compareTo is used, not equals, so scale doesn't matter
			assertTrue(Variant.isEquals(new BigDecimal("10.0"), new BigDecimal("10.00")));
		}

		@Test
		@DisplayName("String equality")
		void stringEquality() {
			assertTrue(Variant.isEquals("abc", new String("abc")));
			assertFalse(Variant.isEquals("abc", "xyz"));
		}

		@Test
		@DisplayName("Date equality")
		void dateEquality() {
			java.sql.Date d1 = java.sql.Date.valueOf("2025-01-01");
			java.sql.Date d2 = java.sql.Date.valueOf("2025-01-01");
			assertTrue(Variant.isEquals(d1, d2));
			assertFalse(Variant.isEquals(d1, java.sql.Date.valueOf("2025-01-02")));
		}

		@Test
		@DisplayName("Boolean equality")
		void booleanEquality() {
			assertTrue(Variant.isEquals(true, true));
			assertTrue(Variant.isEquals(false, false));
			assertFalse(Variant.isEquals(true, false));
		}

		@Test
		@DisplayName("byte array equality")
		void byteArrayEquality() {
			assertTrue(Variant.isEquals(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}));
			assertFalse(Variant.isEquals(new byte[]{1, 2, 3}, new byte[]{1, 2, 4}));
			assertFalse(Variant.isEquals(new byte[]{1, 2}, new byte[]{1, 2, 3}));
		}

		@Test
		@DisplayName("incompatible types -> false (not exception)")
		void incompatibleTypes() {
			assertFalse(Variant.isEquals(42, "42"));
		}
	}

	// ───── compareArrays (Object[]) ─────

	@Nested
	@DisplayName("compareArrays (Object[])")
	class CompareArraysObj {

		@Test
		@DisplayName("equal arrays -> 0")
		void equalArrays() {
			Object[] a = {1, "b", 3L};
			Object[] b = {1, "b", 3L};
			assertEquals(0, Variant.compareArrays(a, b));
		}

		@Test
		@DisplayName("first difference determines result")
		void firstDiff() {
			Object[] a = {1, "a"};
			Object[] b = {1, "b"};
			assertTrue(Variant.compareArrays(a, b) < 0);
		}
	}

	// ───── compareArrays (byte[]) ─────

	@Nested
	@DisplayName("compareArrays (byte[])")
	class CompareArraysBytes {

		@Test
		@DisplayName("equal byte arrays -> 0")
		void equal() {
			assertEquals(0, Variant.compareArrays(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}));
		}

		@Test
		@DisplayName("shorter array is smaller when prefix matches")
		void shorterSmaller() {
			assertTrue(Variant.compareArrays(new byte[]{1, 2}, new byte[]{1, 2, 3}) < 0);
		}

		@Test
		@DisplayName("longer array is larger when prefix matches")
		void longerLarger() {
			assertTrue(Variant.compareArrays(new byte[]{1, 2, 3}, new byte[]{1, 2}) > 0);
		}

		@Test
		@DisplayName("element-wise comparison before length")
		void elementWise() {
			assertTrue(Variant.compareArrays(new byte[]{1, 3}, new byte[]{1, 2, 9}) > 0);
		}
	}

	// ───── round ─────

	@Nested
	@DisplayName("round")
	class Round {

		@Test
		@DisplayName("round null -> null")
		void roundNull() {
			assertNull(Variant.round(null));
		}

		@Test
		@DisplayName("round Double")
		void roundDouble() {
			Object result = Variant.round(3.7);
			assertInstanceOf(Double.class, result);
			assertEquals(4.0, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("round Integer returns same")
		void roundInteger() {
			assertEquals(5, Variant.round(5));
		}

		@Test
		@DisplayName("round BigDecimal")
		void roundBigDecimal() {
			Object result = Variant.round(new BigDecimal("3.56"));
			assertInstanceOf(BigDecimal.class, result);
			assertEquals(0, new BigDecimal("4").compareTo((BigDecimal) result));
		}

		@Test
		@DisplayName("round with scale")
		void roundWithScale() {
			Object result = Variant.round(3.456, 2);
			assertInstanceOf(Double.class, result);
			assertEquals(3.46, ((Double) result).doubleValue(), 0.001);
		}
	}

	// ───── parseInt / parseLong ─────

	@Nested
	@DisplayName("parseInt / parseLong")
	class ParseIntLong {

		@Test
		@DisplayName("parseInt valid integer")
		void parseIntValid() {
			assertEquals(123, Variant.parseInt("123"));
			assertEquals(-456, Variant.parseInt("-456"));
			assertEquals(0, Variant.parseInt("0"));
		}

		@Test
		@DisplayName("parseInt returns null for non-integer")
		void parseIntInvalid() {
			assertNull(Variant.parseInt("abc"));
			assertNull(Variant.parseInt(""));
			assertNull(Variant.parseInt("-"));
		}

		@Test
		@DisplayName("parseLong valid long")
		void parseLongValid() {
			assertEquals(123L, Variant.parseLong("123"));
			assertEquals(-456L, Variant.parseLong("-456"));
		}

		@Test
		@DisplayName("parseLong with L suffix")
		void parseLongWithSuffix() {
			assertEquals(100L, Variant.parseLong("100L"));
		}

		@Test
		@DisplayName("parseLong returns null for non-long")
		void parseLongInvalid() {
			assertNull(Variant.parseLong(""));
			assertNull(Variant.parseLong("-"));
			assertNull(Variant.parseLong("abc"));
		}

		@Test
		@DisplayName("parseLong hex")
		void parseLongHex() {
			assertEquals(255L, Variant.parseLong("FF", 16));
			assertEquals(0L, Variant.parseLong("0", 16));
			assertNull(Variant.parseLong("", 16));
			assertNull(Variant.parseLong("GG", 16));
		}
	}

	// ───── parseNumber ─────

	@Nested
	@DisplayName("parseNumber")
	class ParseNumber {

		@Test
		@DisplayName("null returns null")
		void parseNull() {
			assertNull(Variant.parseNumber(null));
		}

		@Test
		@DisplayName("empty returns null")
		void parseEmpty() {
			assertNull(Variant.parseNumber(""));
			assertNull(Variant.parseNumber("   "));
		}

		@Test
		@DisplayName("parse integer string")
		void parseIntStr() {
			Number n = Variant.parseNumber("42");
			assertInstanceOf(Integer.class, n);
			assertEquals(42, n.intValue());
		}

		@Test
		@DisplayName("parse long string")
		void parseLongStr() {
			// A number too big for int but valid as long
			Number n = Variant.parseNumber("9999999999");
			assertInstanceOf(Long.class, n);
			assertEquals(9999999999L, n.longValue());
		}

		@Test
		@DisplayName("parse double string")
		void parseDoubleStr() {
			Number n = Variant.parseNumber("3.14");
			assertInstanceOf(Double.class, n);
			assertEquals(3.14, n.doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("parse hex string")
		void parseHexStr() {
			Number n = Variant.parseNumber("0xFF");
			assertInstanceOf(Long.class, n);
			assertEquals(255L, n.longValue());
		}

		@Test
		@DisplayName("non-numeric returns null")
		void parseNonNumeric() {
			assertNull(Variant.parseNumber("hello"));
		}
	}

	// ───── getDataType ─────

	@Nested
	@DisplayName("getDataType")
	class GetDataType {

		@Test
		@DisplayName("returns type names for common types")
		void commonTypes() {
			// Just verify it doesn't throw and returns non-null for all common types
			assertNotNull(Variant.getDataType(null));
			assertNotNull(Variant.getDataType("hello"));
			assertNotNull(Variant.getDataType(42));
			assertNotNull(Variant.getDataType(42L));
			assertNotNull(Variant.getDataType(3.14));
			assertNotNull(Variant.getDataType(true));
			assertNotNull(Variant.getDataType(new BigDecimal("1")));
			assertNotNull(Variant.getDataType(new byte[]{1, 2}));
		}
	}

	// ───── getObjectType ─────

	@Nested
	@DisplayName("getObjectType")
	class GetObjectType {

		@Test
		@DisplayName("returns correct type constants")
		void typeConstants() {
			assertEquals(com.scudata.common.Types.DT_STRING, Variant.getObjectType("hello"));
			assertEquals(com.scudata.common.Types.DT_INT, Variant.getObjectType(42));
			assertEquals(com.scudata.common.Types.DT_DOUBLE, Variant.getObjectType(3.14));
			assertEquals(com.scudata.common.Types.DT_LONG, Variant.getObjectType(42L));
			assertEquals(com.scudata.common.Types.DT_DECIMAL, Variant.getObjectType(new BigDecimal("1")));
			assertEquals(com.scudata.common.Types.DT_BOOLEAN, Variant.getObjectType(true));
			assertEquals(com.scudata.common.Types.DT_DATE, Variant.getObjectType(java.sql.Date.valueOf("2025-01-01")));
			assertEquals(com.scudata.common.Types.DT_TIME, Variant.getObjectType(java.sql.Time.valueOf("12:00:00")));
			assertEquals(com.scudata.common.Types.DT_DATETIME, Variant.getObjectType(java.sql.Timestamp.valueOf("2025-01-01 12:00:00")));
		}
	}

	// ───── toString ─────

	@Nested
	@DisplayName("toString")
	class ToStringTests {

		@Test
		@DisplayName("null -> null")
		void toStringNull() {
			assertNull(Variant.toString(null));
		}

		@Test
		@DisplayName("Integer -> string representation")
		void toStringInt() {
			assertEquals("42", Variant.toString(42));
		}

		@Test
		@DisplayName("byte[] -> string")
		void toStringBytes() {
			assertEquals("abc", Variant.toString("abc".getBytes()));
		}
	}

	// ───── canConvertToString ─────

	@Nested
	@DisplayName("canConvertToString")
	class CanConvertToString {

		@Test
		@DisplayName("primitives are convertible")
		void primitivesConvertible() {
			assertTrue(Variant.canConvertToString(42));
			assertTrue(Variant.canConvertToString("hello"));
			assertTrue(Variant.canConvertToString(null));
		}
	}

	// ───── toBigDecimal (public) ─────

	@Nested
	@DisplayName("toBigDecimal")
	class ToBigDecimalTests {

		@Test
		@DisplayName("BigDecimal -> same")
		void fromBigDecimal() {
			BigDecimal bd = new BigDecimal("10.5");
			assertSame(bd, Variant.toBigDecimal(bd));
		}

		@Test
		@DisplayName("BigInteger -> BigDecimal")
		void fromBigInteger() {
			BigInteger bi = BigInteger.valueOf(100);
			BigDecimal result = Variant.toBigDecimal(bi);
			assertEquals(0, new BigDecimal("100").compareTo(result));
		}

		@Test
		@DisplayName("Long -> BigDecimal")
		void fromLong() {
			BigDecimal result = Variant.toBigDecimal(100L);
			assertEquals(0, new BigDecimal("100").compareTo(result));
		}

		@Test
		@DisplayName("Integer -> BigDecimal")
		void fromInteger() {
			BigDecimal result = Variant.toBigDecimal(42);
			assertEquals(42.0, result.doubleValue(), 0.0001);
		}
	}

	// ───── toBigInteger ─────

	@Nested
	@DisplayName("toBigInteger")
	class ToBigIntegerTests {

		@Test
		@DisplayName("BigDecimal -> BigInteger")
		void fromBigDecimal() {
			assertEquals(BigInteger.valueOf(10), Variant.toBigInteger(new BigDecimal("10.9")));
		}

		@Test
		@DisplayName("BigInteger -> same")
		void fromBigInteger() {
			BigInteger bi = BigInteger.valueOf(42);
			assertSame(bi, Variant.toBigInteger(bi));
		}

		@Test
		@DisplayName("Long -> BigInteger")
		void fromLong() {
			assertEquals(BigInteger.valueOf(100), Variant.toBigInteger(100L));
		}
	}

	// ───── add1 ─────

	@Nested
	@DisplayName("add1")
	class Add1 {

		@Test
		@DisplayName("add1(null) returns Integer(1)")
		void add1Null() {
			Object result = Variant.add1(null);
			assertInstanceOf(Integer.class, result);
			assertEquals(1, result);
		}

		@Test
		@DisplayName("add1(Integer) -> Long (overflow protection)")
		void add1Int() {
			Object result = Variant.add1(5);
			assertInstanceOf(Long.class, result);
			assertEquals(6L, result);
		}
	}

	// ───── square ─────

	@Nested
	@DisplayName("square")
	class Square {

		@Test
		@DisplayName("square null -> null")
		void squareNull() {
			assertNull(Variant.square(null));
		}

		@Test
		@DisplayName("square Integer -> Integer")
		void squareInt() {
			Object result = Variant.square(5);
			assertInstanceOf(Integer.class, result);
			assertEquals(25, result);
		}

		@Test
		@DisplayName("square Double -> Double")
		void squareDouble() {
			Object result = Variant.square(3.0);
			assertInstanceOf(Double.class, result);
			assertEquals(9.0, ((Double) result).doubleValue(), 0.0001);
		}
	}

	// ───── intDivide ─────

	@Nested
	@DisplayName("intDivide")
	class IntDivide {

		@Test
		@DisplayName("int / int -> Integer")
		void intDivInt() {
			Number result = Variant.intDivide((Object) 10, (Object) 3);
			assertInstanceOf(Integer.class, result);
			assertEquals(3, result);
		}

		@Test
		@DisplayName("null -> null")
		void intDivNull() {
			assertNull(Variant.intDivide((Object) null, (Object) 3));
			assertNull(Variant.intDivide((Object) 10, (Object) null));
		}

		@Test
		@DisplayName("Double uses longValue for integer division")
		void intDivDouble() {
			Number result = Variant.intDivide((Object) 10.9, (Object) 3.0);
			assertInstanceOf(Long.class, result);
			assertEquals(3L, result);
		}
	}

	// ───── dayInterval / secondInterval ─────

	@Nested
	@DisplayName("dayInterval / secondInterval")
	class DateIntervals {

		@Test
		@DisplayName("dayInterval between two dates")
		void dayInterval() {
			java.sql.Date d1 = java.sql.Date.valueOf("2025-01-01");
			java.sql.Date d2 = java.sql.Date.valueOf("2025-01-11");
			assertEquals(10L, Variant.dayInterval(d1, d2));
			assertEquals(-10L, Variant.dayInterval(d2, d1));
		}

		@Test
		@DisplayName("secondInterval between two timestamps")
		void secondInterval() {
			java.sql.Timestamp t1 = java.sql.Timestamp.valueOf("2025-01-01 00:00:00");
			java.sql.Timestamp t2 = java.sql.Timestamp.valueOf("2025-01-01 01:00:00");
			assertEquals(3600L, Variant.secondInterval(t1, t2));
		}
	}

	// ───── and (bitwise) ─────

	@Nested
	@DisplayName("and (bitwise)")
	class And {

		@Test
		@DisplayName("Integer AND Integer")
		void andIntInt() {
			Number result = Variant.and(0xFF, 0x0F);
			assertEquals(0x0F, result.intValue());
		}

		@Test
		@DisplayName("Long AND Long")
		void andLongLong() {
			Number result = Variant.and(0xFFL, 0x0FL);
			assertEquals(0x0FL, result.longValue());
		}

		@Test
		@DisplayName("BigInteger AND BigInteger")
		void andBigIntBigInt() {
			BigInteger a = BigInteger.valueOf(0xFF);
			BigInteger b = BigInteger.valueOf(0x0F);
			Number result = Variant.and(a, b);
			assertEquals(BigInteger.valueOf(0x0F), result);
		}
	}

	// ───── parse ─────

	@Nested
	@DisplayName("parse")
	class Parse {

		@Test
		@DisplayName("null returns null")
		void parseNull() {
			assertNull(Variant.parse(null));
		}

		@Test
		@DisplayName("empty returns null")
		void parseEmpty() {
			assertNull(Variant.parse(""));
		}

		@Test
		@DisplayName("whitespace-only returns original text")
		void parseWhitespace() {
			assertEquals("   ", Variant.parse("   "));
		}

		@Test
		@DisplayName("integer string")
		void parseInt() {
			Object result = Variant.parse("42");
			assertInstanceOf(Integer.class, result);
			assertEquals(42, result);
		}

		@Test
		@DisplayName("'null' string returns null")
		void parseNullString() {
			assertNull(Variant.parse("null"));
		}

		@Test
		@DisplayName("'true' returns Boolean.TRUE")
		void parseTrueString() {
			assertEquals(Boolean.TRUE, Variant.parse("true"));
		}

		@Test
		@DisplayName("'false' returns Boolean.FALSE")
		void parseFalseString() {
			assertEquals(Boolean.FALSE, Variant.parse("false"));
		}

		@Test
		@DisplayName("hex string")
		void parseHex() {
			Object result = Variant.parse("0xFF");
			assertInstanceOf(Long.class, result);
			assertEquals(255L, result);
		}
	}

	// ───── elapse (date shift) ─────

	@Nested
	@DisplayName("elapse")
	class Elapse {

		@Test
		@DisplayName("elapse by days (null opt)")
		void elapseByDays() {
			java.sql.Date date = java.sql.Date.valueOf("2025-01-01");
			Date result = Variant.elapse(date, 10, null);
			Calendar c = Calendar.getInstance();
			c.setTime(result);
			assertEquals(11, c.get(Calendar.DAY_OF_MONTH));
		}

		@Test
		@DisplayName("elapse by months")
		void elapseByMonths() {
			java.sql.Date date = java.sql.Date.valueOf("2025-01-15");
			Date result = Variant.elapse(date, 2, "m");
			Calendar c = Calendar.getInstance();
			c.setTime(result);
			assertEquals(Calendar.MARCH, c.get(Calendar.MONTH));
			assertEquals(15, c.get(Calendar.DAY_OF_MONTH));
		}

		@Test
		@DisplayName("elapse by years")
		void elapseByYears() {
			java.sql.Date date = java.sql.Date.valueOf("2025-06-15");
			Date result = Variant.elapse(date, 1, "y");
			Calendar c = Calendar.getInstance();
			c.setTime(result);
			assertEquals(2026, c.get(Calendar.YEAR));
		}

		@Test
		@DisplayName("elapse by seconds")
		void elapseBySeconds() {
			java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2025-01-01 12:00:00");
			Date result = Variant.elapse(ts, 3600, "s");
			Calendar c = Calendar.getInstance();
			c.setTime(result);
			assertEquals(13, c.get(Calendar.HOUR_OF_DAY));
		}
	}

	// ───── interval ─────

	@Nested
	@DisplayName("interval")
	class Interval {

		@Test
		@DisplayName("day interval (null opt)")
		void dayInterval() {
			java.sql.Date d1 = java.sql.Date.valueOf("2025-01-01");
			java.sql.Date d2 = java.sql.Date.valueOf("2025-02-01");
			assertEquals(31L, Variant.interval(d1, d2, null));
		}

		@Test
		@DisplayName("month interval")
		void monthInterval() {
			java.sql.Date d1 = java.sql.Date.valueOf("2025-01-15");
			java.sql.Date d2 = java.sql.Date.valueOf("2025-04-15");
			assertEquals(3L, Variant.interval(d1, d2, "m"));
		}

		@Test
		@DisplayName("year interval")
		void yearInterval() {
			java.sql.Date d1 = java.sql.Date.valueOf("2020-06-01");
			java.sql.Date d2 = java.sql.Date.valueOf("2025-06-01");
			assertEquals(5L, Variant.interval(d1, d2, "y"));
		}

		@Test
		@DisplayName("second interval")
		void secondInterval() {
			java.sql.Timestamp t1 = java.sql.Timestamp.valueOf("2025-01-01 00:00:00");
			java.sql.Timestamp t2 = java.sql.Timestamp.valueOf("2025-01-01 01:30:00");
			assertEquals(5400L, Variant.interval(t1, t2, "s"));
		}
	}

	// ───── CONSTANTS ─────

	@Test
	@DisplayName("constants are correct")
	void constants() {
		assertEquals(1, Variant.DT_INT);
		assertEquals(2, Variant.DT_LONG);
		assertEquals(3, Variant.DT_DOUBLE);
		assertEquals(4, Variant.DT_DECIMAL);
		assertEquals(16, Variant.Divide_Scale);
		assertEquals(BigDecimal.ROUND_HALF_UP, Variant.Divide_Round);
		assertTrue(Variant.INFINITY.isInfinite());
	}

	// ───── convert ─────

	@Nested
	@DisplayName("convert")
	class Convert {

		@Test
		@DisplayName("convert null -> null")
		void convertNull() {
			assertNull(Variant.convert(null, com.scudata.common.Types.DT_INT));
		}

		@Test
		@DisplayName("convert Integer to Long")
		void intToLong() {
			Object result = Variant.convert(42, com.scudata.common.Types.DT_LONG);
			assertInstanceOf(Long.class, result);
			assertEquals(42L, result);
		}

		@Test
		@DisplayName("convert Long to Integer")
		void longToInt() {
			Object result = Variant.convert(42L, com.scudata.common.Types.DT_INT);
			assertInstanceOf(Integer.class, result);
			assertEquals(42, result);
		}

		@Test
		@DisplayName("convert Integer to Double")
		void intToDouble() {
			Object result = Variant.convert(42, com.scudata.common.Types.DT_DOUBLE);
			assertInstanceOf(Double.class, result);
			assertEquals(42.0, ((Double) result).doubleValue(), 0.0001);
		}

		@Test
		@DisplayName("convert Integer to String")
		void intToString() {
			Object result = Variant.convert(42, com.scudata.common.Types.DT_STRING);
			assertEquals("42", result);
		}
	}

	// ───── getBaseDate ─────

	@Test
	@DisplayName("getBaseDate returns a reasonable value (year 2000)")
	void getBaseDate() {
		long baseDate = Variant.getBaseDate();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(baseDate);
		assertEquals(2000, c.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
		assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
	}
}
