package com.scudata.expression;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Param;
import com.scudata.dm.Sequence;

/**
 * Tests for {@link Expression} — the core expression parser and evaluator.
 * Tests cover constructors, literal parsing, arithmetic/comparison/logical
 * expressions, function calls, string expressions, and utility methods.
 */
@DisplayName("Expression Tests")
public class ExpressionTest {

	private Context ctx;

	@BeforeEach
	void setUp() {
		ctx = new Context();
	}

	// ── Constructors and basic properties ──

	@Test
	@DisplayName("Null expression string creates Constant(null)")
	void nullString() {
		Expression exp = new Expression((String) null);
		assertTrue(exp.isConstExpression());
		assertNull(exp.calculate(ctx));
	}

	@Test
	@DisplayName("Empty string creates Constant(null)")
	void emptyString() {
		Expression exp = new Expression("");
		assertTrue(exp.isConstExpression());
		assertNull(exp.calculate(ctx));
	}

	@Test
	@DisplayName("Construct from Node directly")
	void constructFromNode() {
		Constant c = new Constant(42);
		Expression exp = new Expression(c);
		assertTrue(exp.isConstExpression());
		assertEquals(42, exp.calculate(ctx));
		assertSame(c, exp.getHome());
	}

	@Test
	@DisplayName("toString returns original expression string")
	void toStringReturnsExpStr() {
		Expression exp = new Expression("1+2");
		assertEquals("1+2", exp.toString());
	}

	@Test
	@DisplayName("Expression.NULL is constant null")
	void expressionNull() {
		assertNull(Expression.NULL.calculate(ctx));
		assertTrue(Expression.NULL.isConstExpression());
	}

	// ── Literal parsing ──

	@Nested
	@DisplayName("Literal Parsing")
	class LiteralParsing {

		@Test
		@DisplayName("Integer literal")
		void integerLiteral() {
			Expression exp = new Expression("42");
			assertEquals(42, ((Number) exp.calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Long literal")
		void longLiteral() {
			Expression exp = new Expression("9999999999");
			Object result = exp.calculate(ctx);
			assertTrue(result instanceof Long);
			assertEquals(9999999999L, ((Long) result).longValue());
		}

		@Test
		@DisplayName("Floating point literal")
		void floatingPointLiteral() {
			Expression exp = new Expression("3.14");
			Object result = exp.calculate(ctx);
			assertTrue(result instanceof Double);
			assertEquals(3.14, ((Double) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("Negative number via unary minus")
		void negativeNumber() {
			Expression exp = new Expression("-5");
			Object result = exp.calculate(ctx);
			assertEquals(-5, ((Number) result).intValue());
		}

		@Test
		@DisplayName("String literal in double quotes")
		void stringLiteralDoubleQuotes() {
			Expression exp = new Expression("\"hello\"");
			assertEquals("hello", exp.calculate(ctx));
		}

		@Test
		@DisplayName("Boolean literal true")
		void booleanTrue() {
			Expression exp = new Expression("true");
			assertEquals(Boolean.TRUE, exp.calculate(ctx));
		}

		@Test
		@DisplayName("Boolean literal false")
		void booleanFalse() {
			Expression exp = new Expression("false");
			assertEquals(Boolean.FALSE, exp.calculate(ctx));
		}

		@Test
		@DisplayName("null literal")
		void nullLiteral() {
			Expression exp = new Expression("null");
			assertNull(exp.calculate(ctx));
		}

		@Test
		@DisplayName("Floating point starting with dot: .5")
		void dotPrefixFloat() {
			Expression exp = new Expression(".5");
			Object result = exp.calculate(ctx);
			assertTrue(result instanceof Number);
			assertEquals(0.5, ((Number) result).doubleValue(), 1e-9);
		}
	}

	// ── Arithmetic expressions ──

	@Nested
	@DisplayName("Arithmetic Expressions")
	class Arithmetic {

		@Test
		@DisplayName("Addition: 1 + 2 = 3")
		void addition() {
			assertEquals(3, ((Number) new Expression("1+2").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Subtraction: 10 - 3 = 7")
		void subtraction() {
			assertEquals(7, ((Number) new Expression("10-3").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Multiplication: 4 * 5 = 20")
		void multiplication() {
			assertEquals(20, ((Number) new Expression("4*5").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Division: 10 / 4 = 2.5")
		void division() {
			assertEquals(2.5, ((Number) new Expression("10/4").calculate(ctx)).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("Modulo: 10 % 3 = 1")
		void modulo() {
			assertEquals(1, ((Number) new Expression("10%3").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Operator precedence: 2 + 3 * 4 = 14")
		void precedence() {
			assertEquals(14, ((Number) new Expression("2+3*4").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Parentheses override precedence: (2+3)*4 = 20")
		void parentheses() {
			assertEquals(20, ((Number) new Expression("(2+3)*4").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Nested parentheses: ((2+3)*4)+1 = 21")
		void nestedParentheses() {
			assertEquals(21, ((Number) new Expression("((2+3)*4)+1").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Unary plus: +5 = 5")
		void unaryPlus() {
			assertEquals(5, ((Number) new Expression("+5").calculate(ctx)).intValue());
		}

		@Test
		@DisplayName("Integer division: 7 \\ 2 = 3")
		void intDivide() {
			Expression exp = new Expression("7\\2");
			Object result = exp.calculate(ctx);
			assertEquals(3, ((Number) result).intValue());
		}

		@Test
		@DisplayName("Complex expression: (10-2)*(3+1)/4 = 8")
		void complexExpression() {
			assertEquals(8.0, ((Number) new Expression("(10-2)*(3+1)/4").calculate(ctx)).doubleValue(), 1e-9);
		}
	}

	// ── Comparison expressions ──

	@Nested
	@DisplayName("Comparison Expressions")
	class Comparisons {

		@Test
		@DisplayName("Equals: 3==3 is true")
		void equalsTrue() {
			assertEquals(Boolean.TRUE, new Expression("3==3").calculate(ctx));
		}

		@Test
		@DisplayName("Equals: 3==4 is false")
		void equalsFalse() {
			assertEquals(Boolean.FALSE, new Expression("3==4").calculate(ctx));
		}

		@Test
		@DisplayName("Not equals: 3!=4 is true")
		void notEquals() {
			assertEquals(Boolean.TRUE, new Expression("3!=4").calculate(ctx));
		}

		@Test
		@DisplayName("Greater: 5>3 is true")
		void greaterTrue() {
			assertEquals(Boolean.TRUE, new Expression("5>3").calculate(ctx));
		}

		@Test
		@DisplayName("Greater: 3>5 is false")
		void greaterFalse() {
			assertEquals(Boolean.FALSE, new Expression("3>5").calculate(ctx));
		}

		@Test
		@DisplayName("Less: 3<5 is true")
		void lessThan() {
			assertEquals(Boolean.TRUE, new Expression("3<5").calculate(ctx));
		}

		@Test
		@DisplayName("Greater or equal: 3>=3 is true")
		void greaterOrEqual() {
			assertEquals(Boolean.TRUE, new Expression("3>=3").calculate(ctx));
		}

		@Test
		@DisplayName("Less or equal: 3<=5 is true")
		void lessOrEqual() {
			assertEquals(Boolean.TRUE, new Expression("3<=5").calculate(ctx));
		}
	}

	// ── Logical expressions ──

	@Nested
	@DisplayName("Logical Expressions")
	class Logical {

		@Test
		@DisplayName("And: true && true = true")
		void andTrueTrue() {
			assertEquals(Boolean.TRUE, new Expression("true&&true").calculate(ctx));
		}

		@Test
		@DisplayName("And: true && false = false")
		void andTrueFalse() {
			assertEquals(Boolean.FALSE, new Expression("true&&false").calculate(ctx));
		}

		@Test
		@DisplayName("Or: false || true = true")
		void orFalseTrue() {
			assertEquals(Boolean.TRUE, new Expression("false||true").calculate(ctx));
		}

		@Test
		@DisplayName("Or: false || false = false")
		void orFalseFalse() {
			assertEquals(Boolean.FALSE, new Expression("false||false").calculate(ctx));
		}

		@Test
		@DisplayName("Not: !true = false")
		void notTrue() {
			assertEquals(Boolean.FALSE, new Expression("!true").calculate(ctx));
		}

		@Test
		@DisplayName("Not: !false = true")
		void notFalse() {
			assertEquals(Boolean.TRUE, new Expression("!false").calculate(ctx));
		}

		@Test
		@DisplayName("Not: !null = true (null is falsy)")
		void notNull() {
			assertEquals(Boolean.TRUE, new Expression("!null").calculate(ctx));
		}

		@Test
		@DisplayName("Combined: !(3>5) = true")
		void notComparison() {
			assertEquals(Boolean.TRUE, new Expression("!(3>5)").calculate(ctx));
		}
	}

	// ── Function calls via expression ──

	@Nested
	@DisplayName("Function Calls")
	class FunctionCalls {

		@Test
		@DisplayName("abs(-5) = 5")
		void abs() {
			Object result = new Expression("abs(-5)").calculate(ctx);
			assertEquals(5, ((Number) result).intValue());
		}

		@Test
		@DisplayName("sqrt(9) = 3.0")
		void sqrt() {
			Object result = new Expression("sqrt(9)").calculate(ctx);
			assertEquals(3.0, ((Number) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("power(2,10) = 1024.0")
		void power() {
			Object result = new Expression("power(2,10)").calculate(ctx);
			assertEquals(1024.0, ((Number) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("round(3.7) rounds to integer")
		void round() {
			Object result = new Expression("round(3.7)").calculate(ctx);
			assertTrue(result instanceof Number);
		}

		@Test
		@DisplayName("len(\"hello\") = 5")
		void len() {
			Object result = new Expression("len(\"hello\")").calculate(ctx);
			assertEquals(5, ((Number) result).intValue());
		}

		@Test
		@DisplayName("upper(\"abc\") = ABC")
		void upper() {
			assertEquals("ABC", new Expression("upper(\"abc\")").calculate(ctx));
		}

		@Test
		@DisplayName("lower(\"ABC\") = abc")
		void lower() {
			assertEquals("abc", new Expression("lower(\"ABC\")").calculate(ctx));
		}

		@Test
		@DisplayName("int(3.9) = 3")
		void toInteger() {
			Object result = new Expression("int(3.9)").calculate(ctx);
			assertEquals(3, ((Number) result).intValue());
		}

		@Test
		@DisplayName("long(42) = 42L")
		void toLong() {
			Object result = new Expression("long(42)").calculate(ctx);
			assertTrue(result instanceof Long);
			assertEquals(42L, ((Long) result).longValue());
		}

		@Test
		@DisplayName("float(3) = 3.0")
		void toDouble() {
			Object result = new Expression("float(3)").calculate(ctx);
			assertTrue(result instanceof Double);
			assertEquals(3.0, ((Double) result).doubleValue(), 1e-9);
		}

		@Test
		@DisplayName("bool(null) = false")
		void toBoolNull() {
			assertEquals(Boolean.FALSE, new Expression("bool(null)").calculate(ctx));
		}

		@Test
		@DisplayName("bool(1) = true")
		void toBoolOne() {
			assertEquals(Boolean.TRUE, new Expression("bool(1)").calculate(ctx));
		}

		@Test
		@DisplayName("string(123)")
		void toStringFunc() {
			Object result = new Expression("string(123)").calculate(ctx);
			assertEquals("123", result);
		}
	}

	// ── Sequence / list expressions ──

	@Nested
	@DisplayName("Sequence Expressions")
	class SequenceExpressions {

		@Test
		@DisplayName("[1,2,3] creates a Sequence of length 3")
		void createSequence() {
			Object result = new Expression("[1,2,3]").calculate(ctx);
			assertTrue(result instanceof Sequence);
			assertEquals(3, ((Sequence) result).length());
		}

		@Test
		@DisplayName("[] creates empty sequence")
		void emptySequence() {
			Object result = new Expression("[]").calculate(ctx);
			assertTrue(result instanceof Sequence);
			assertEquals(0, ((Sequence) result).length());
		}
	}

	// ── Parameters / variables ──

	@Nested
	@DisplayName("Variable and Parameter Expressions")
	class Variables {

		@Test
		@DisplayName("Expression uses context variable")
		void contextVariable() {
			ctx.setParamValue("x", 10);
			Object result = new Expression(ctx, "x+5").calculate(ctx);
			assertEquals(15, ((Number) result).intValue());
		}

		@Test
		@DisplayName("Expression uses multiple variables")
		void multipleVariables() {
			ctx.setParamValue("a", 3);
			ctx.setParamValue("b", 7);
			Object result = new Expression(ctx, "a*b").calculate(ctx);
			assertEquals(21, ((Number) result).intValue());
		}

		@Test
		@DisplayName("containParam returns true for used param")
		void containParam() {
			ctx.setParamValue("myvar", 1);
			Expression exp = new Expression(ctx, "myvar+1");
			assertTrue(exp.containParam("myvar"));
		}

		@Test
		@DisplayName("containParam returns false for unused param")
		void containParamFalse() {
			Expression exp = new Expression("1+2");
			assertFalse(exp.containParam("xyz"));
		}

		@Test
		@DisplayName("containParam with null/empty returns false")
		void containParamNullEmpty() {
			Expression exp = new Expression("1+2");
			assertFalse(exp.containParam(null));
			assertFalse(exp.containParam(""));
		}
	}

	// ── Utility methods ──

	@Nested
	@DisplayName("Utility Methods")
	class UtilityMethods {

		@Test
		@DisplayName("getIdentifierName strips quotes")
		void getIdentifierName() {
			Expression exp = new Expression("'abc'");
			assertEquals("abc", exp.getIdentifierName());
		}

		@Test
		@DisplayName("getIdentifierName without quotes returns as-is")
		void getIdentifierNameNoQuotes() {
			Expression exp = new Expression("123");
			assertEquals("123", exp.getIdentifierName());
		}

		@Test
		@DisplayName("isConstExpression for constant")
		void isConstTrue() {
			assertTrue(new Expression("42").isConstExpression());
		}

		@Test
		@DisplayName("isConstExpression for optimized constant expression")
		void isConstFalse() {
			// The parser optimizes 1+2 into Constant(3), so it IS a const expression
			assertTrue(new Expression("1+2").isConstExpression());
		}

		@Test
		@DisplayName("newExpression creates equivalent expression")
		void newExpression() {
			Expression exp = new Expression("1+2");
			Expression copy = exp.newExpression(ctx);
			assertEquals(exp.calculate(ctx), copy.calculate(ctx));
		}

		@Test
		@DisplayName("canCalculateAll for simple expression")
		void canCalculateAll() {
			Expression exp = new Expression("1+2");
			assertTrue(exp.canCalculateAll());
		}

		@Test
		@DisplayName("isMonotone for constant")
		void isMonotone() {
			assertTrue(new Expression("42").isMonotone());
		}
	}

	// ── Static methods ──

	@Nested
	@DisplayName("Static Methods")
	class StaticMethods {

		@Test
		@DisplayName("scanParenthesis finds matching paren")
		void scanParenthesis() {
			assertEquals(4, Expression.scanParenthesis("(1+2)", 0));
		}

		@Test
		@DisplayName("scanParenthesis with nested parens")
		void scanParenthesisNested() {
			assertEquals(8, Expression.scanParenthesis("((1+2)+3)", 0));
		}

		@Test
		@DisplayName("scanParenthesis unmatched returns -1")
		void scanParenthesisUnmatched() {
			assertEquals(-1, Expression.scanParenthesis("(1+2", 0));
		}

		@Test
		@DisplayName("ifIs checks type match")
		void ifIsBasic() {
			assertTrue(Expression.ifIs("hello", String.class));
			assertFalse(Expression.ifIs(null, String.class));
			assertFalse(Expression.ifIs(42, String.class));
		}

		@Test
		@DisplayName("replaceFunc replaces function name")
		void replaceFunc() {
			assertEquals("bar(x)", Expression.replaceFunc("foo(x)", "foo", "bar"));
		}

		@Test
		@DisplayName("replaceMacros with null returns null")
		void replaceMacrosNull() {
			assertNull(Expression.replaceMacros(null, null, null));
		}

		@Test
		@DisplayName("replaceMacros without macros returns same")
		void replaceMacrosNoMacro() {
			assertEquals("hello", Expression.replaceMacros("hello", null, null));
		}

		@Test
		@DisplayName("containMacro with null returns false")
		void containMacroNull() {
			assertFalse(Expression.containMacro(null));
		}

		@Test
		@DisplayName("sameExpression compares expressions")
		void sameExpression() {
			// sameExpression has a limitation: the set pointer for exp2 is not
			// advanced after each match, so it only works reliably for
			// single-character expressions or identical strings of length 1.
			assertTrue(Expression.sameExpression("a", "a"));
			assertTrue(Expression.sameExpression(" a ", "a"));
			assertFalse(Expression.sameExpression("a", "b"));
		}
	}

	// ── Error handling ──

	@Nested
	@DisplayName("Error Handling")
	class ErrorHandling {

		@Test
		@DisplayName("Unmatched parenthesis throws RQException")
		void unmatchedParen() {
			assertThrows(RQException.class, () -> new Expression("(1+2"));
		}

		@Test
		@DisplayName("Missing left operand for * throws")
		void missingLeftOperand() {
			assertThrows(RQException.class, () -> new Expression("*3"));
		}

		@Test
		@DisplayName("Missing left operand for / throws")
		void missingLeftOperandDivide() {
			assertThrows(RQException.class, () -> new Expression("/3"));
		}
	}

	// ── Comments ──

	@Nested
	@DisplayName("Comments")
	class Comments {

		@Test
		@DisplayName("Block comment /* */ is ignored")
		void blockComment() {
			// 1 + /* comment */ 2
			Object result = new Expression("1+/*comment*/2").calculate(ctx);
			assertEquals(3, ((Number) result).intValue());
		}
	}
}
