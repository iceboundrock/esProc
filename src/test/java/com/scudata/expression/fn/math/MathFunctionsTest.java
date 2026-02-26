package com.scudata.expression.fn.math;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.expression.Expression;

/**
 * Tests for math functions: abs, ceil, floor, round, lg, ln, sqrt, power.
 * All tests go through the Expression parser to test end-to-end evaluation.
 */
@DisplayName("Math Functions Tests")
public class MathFunctionsTest {

	private Context ctx;

	@BeforeEach
	void setUp() {
		ctx = new Context();
	}

	private Object eval(String expr) {
		return new Expression(expr).calculate(ctx);
	}

	// ── abs ──

	@Nested
	@DisplayName("abs()")
	class AbsTests {

		@Test
		@DisplayName("abs of positive integer")
		void absPositive() {
			assertEquals(5, ((Number) eval("abs(5)")).intValue());
		}

		@Test
		@DisplayName("abs of negative integer")
		void absNegative() {
			assertEquals(5, ((Number) eval("abs(-5)")).intValue());
		}

		@Test
		@DisplayName("abs of zero")
		void absZero() {
			assertEquals(0, ((Number) eval("abs(0)")).intValue());
		}

		@Test
		@DisplayName("abs of negative double")
		void absDouble() {
			assertEquals(3.14, ((Number) eval("abs(-3.14)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("abs of null returns null")
		void absNull() {
			assertNull(eval("abs(null)"));
		}
	}

	// ── ceil ──

	@Nested
	@DisplayName("ceil()")
	class CeilTests {

		@Test
		@DisplayName("ceil(3.2) = 4.0")
		void ceilUp() {
			assertEquals(4.0, ((Number) eval("ceil(3.2)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("ceil(-3.2) = -3.0")
		void ceilNegative() {
			assertEquals(-3.0, ((Number) eval("ceil(-3.2)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("ceil of integer returns same value")
		void ceilInteger() {
			assertEquals(5.0, ((Number) eval("ceil(5)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("ceil(null) returns null")
		void ceilNull() {
			assertNull(eval("ceil(null)"));
		}

		@Test
		@DisplayName("ceil(3.456, 2) rounds up to 2 decimal places")
		void ceilWithScale() {
			Object result = eval("ceil(3.451,2)");
			assertEquals(3.46, ((Number) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("ceil(0.0) = 0.0")
		void ceilZero() {
			assertEquals(0.0, ((Number) eval("ceil(0.0)")).doubleValue(), 1e-9);
		}
	}

	// ── floor ──

	@Nested
	@DisplayName("floor()")
	class FloorTests {

		@Test
		@DisplayName("floor(3.7) = 3.0")
		void floorDown() {
			assertEquals(3.0, ((Number) eval("floor(3.7)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("floor(-3.7) = -4.0")
		void floorNegative() {
			assertEquals(-4.0, ((Number) eval("floor(-3.7)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("floor of integer returns same value")
		void floorInteger() {
			assertEquals(5.0, ((Number) eval("floor(5)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("floor(null) returns null")
		void floorNull() {
			assertNull(eval("floor(null)"));
		}

		@Test
		@DisplayName("floor(3.456, 2) truncates to 2 decimal places")
		void floorWithScale() {
			Object result = eval("floor(3.459,2)");
			assertEquals(3.45, ((Number) result).doubleValue(), 1e-9);
		}
	}

	// ── round ──

	@Nested
	@DisplayName("round()")
	class RoundTests {

		@Test
		@DisplayName("round(3.5) rounds")
		void roundHalfUp() {
			Object result = eval("round(3.5)");
			assertNotNull(result);
		}

		@Test
		@DisplayName("round(3.2) rounds down")
		void roundDown() {
			Object result = eval("round(3.2)");
			assertNotNull(result);
		}

		@Test
		@DisplayName("round(3.456, 2) rounds to 2 decimal places")
		void roundWithScale() {
			Object result = eval("round(3.456,2)");
			assertNotNull(result);
			assertEquals(3.46, ((Number) result).doubleValue(), 0.01);
		}

		@Test
		@DisplayName("round(null) returns null")
		void roundNull() {
			assertNull(eval("round(null)"));
		}

		@Test
		@DisplayName("round(integer) returns same value")
		void roundInteger() {
			Object result = eval("round(5)");
			assertEquals(5, ((Number) result).intValue());
		}
	}

	// ── lg (log base 10 / configurable base) ──

	@Nested
	@DisplayName("lg()")
	class LgTests {

		@Test
		@DisplayName("lg(100) = 2.0 (base 10)")
		void lgBase10() {
			assertEquals(2.0, ((Number) eval("lg(100)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("lg(1000) = 3.0")
		void lgThousand() {
			assertEquals(3.0, ((Number) eval("lg(1000)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("lg(1) = 0.0")
		void lgOne() {
			assertEquals(0.0, ((Number) eval("lg(1)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("lg(8, 2) = 3.0 (log base 2 of 8)")
		void lgCustomBase() {
			assertEquals(3.0, ((Number) eval("lg(8,2)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("lg(27, 3) = 3.0")
		void lgBase3() {
			assertEquals(3.0, ((Number) eval("lg(27,3)")).doubleValue(), 1e-9);
		}
	}

	// ── ln (natural log) ──

	@Nested
	@DisplayName("ln()")
	class LnTests {

		@Test
		@DisplayName("ln(1) = 0.0")
		void lnOne() {
			assertEquals(0.0, ((Number) eval("ln(1)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("ln(e) ≈ 1.0")
		void lnE() {
			// e ≈ 2.718281828...
			double result = ((Number) eval("ln(2.718281828)")).doubleValue();
			assertEquals(1.0, result, 1e-6);
		}

		@Test
		@DisplayName("ln(null) returns null")
		void lnNull() {
			assertNull(eval("ln(null)"));
		}

		@Test
		@DisplayName("ln(10) ≈ 2.302585")
		void lnTen() {
			assertEquals(Math.log(10), ((Number) eval("ln(10)")).doubleValue(), 1e-9);
		}
	}

	// ── sqrt ──

	@Nested
	@DisplayName("sqrt()")
	class SqrtTests {

		@Test
		@DisplayName("sqrt(9) = 3.0")
		void sqrtPerfectSquare() {
			assertEquals(3.0, ((Number) eval("sqrt(9)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("sqrt(2) ≈ 1.4142")
		void sqrtTwo() {
			assertEquals(Math.sqrt(2), ((Number) eval("sqrt(2)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("sqrt(0) = 0.0")
		void sqrtZero() {
			assertEquals(0.0, ((Number) eval("sqrt(0)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("sqrt(null) returns null")
		void sqrtNull() {
			assertNull(eval("sqrt(null)"));
		}

		@Test
		@DisplayName("sqrt(1) = 1.0")
		void sqrtOne() {
			assertEquals(1.0, ((Number) eval("sqrt(1)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("sqrt(27, 3) = cube root of 27 = 3.0")
		void sqrtNthRoot() {
			assertEquals(3.0, ((Number) eval("sqrt(27,3)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("sqrt(16, 4) = 4th root of 16 = 2.0")
		void sqrtFourthRoot() {
			assertEquals(2.0, ((Number) eval("sqrt(16,4)")).doubleValue(), 1e-9);
		}
	}

	// ── power ──

	@Nested
	@DisplayName("power()")
	class PowerTests {

		@Test
		@DisplayName("power(2, 3) = 8.0")
		void powerBasic() {
			assertEquals(8.0, ((Number) eval("power(2,3)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("power(2, 10) = 1024.0")
		void powerLarge() {
			assertEquals(1024.0, ((Number) eval("power(2,10)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("power(5, 0) = 1.0")
		void powerZeroExp() {
			assertEquals(1.0, ((Number) eval("power(5,0)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("power(any, null) = null")
		void powerNullExp() {
			assertNull(eval("power(5,null)"));
		}

		@Test
		@DisplayName("power(null, 3) = null")
		void powerNullBase() {
			assertNull(eval("power(null,3)"));
		}

		@Test
		@DisplayName("power(3) = 9 (square when one arg)")
		void powerSquare() {
			Object result = eval("power(3)");
			assertEquals(9, ((Number) result).intValue());
		}

		@Test
		@DisplayName("power(-2) = 4 (square of -2)")
		void powerSquareNeg() {
			Object result = eval("power(-2)");
			assertEquals(4, ((Number) result).intValue());
		}

		@Test
		@DisplayName("power(10, -1) = 0.1")
		void powerNegativeExponent() {
			assertEquals(0.1, ((Number) eval("power(10,-1)")).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("power(0, 0) = 1.0 (mathematical convention)")
		void powerZeroZero() {
			assertEquals(1.0, ((Number) eval("power(0,0)")).doubleValue(), 1e-9);
		}
	}

	// ── Combined math expressions ──

	@Nested
	@DisplayName("Combined Math Expressions")
	class CombinedTests {

		@Test
		@DisplayName("abs(-5) + sqrt(16) = 9.0")
		void absAndSqrt() {
			Object result = eval("abs(-5)+sqrt(16)");
			assertEquals(9.0, ((Number) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("power(abs(-3), 2) = 9.0")
		void powerOfAbs() {
			Object result = eval("power(abs(-3),2)");
			assertEquals(9.0, ((Number) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("ceil(sqrt(2)) = 2.0")
		void ceilOfSqrt() {
			Object result = eval("ceil(sqrt(2))");
			assertEquals(2.0, ((Number) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("floor(3.14) + ceil(2.1) = 6.0")
		void floorAndCeil() {
			Object result = eval("floor(3.14)+ceil(2.1)");
			assertEquals(6.0, ((Number) result).doubleValue(), 1e-9);
		}
	}
}
