package com.scudata.expression.fn.string;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;

/**
 * Tests for string functions: trim, upper, lower, left, right, mid, len,
 * replace, and concat.
 * Split is a member function (str.split()) and tested separately.
 * All tests go through the Expression parser for end-to-end evaluation.
 */
@DisplayName("String Functions Tests")
public class StringFunctionsTest {

	private Context ctx;

	@BeforeEach
	void setUp() {
		ctx = new Context();
	}

	private Object eval(String expr) {
		return new Expression(expr).calculate(ctx);
	}

	// ── trim ──

	@Nested
	@DisplayName("trim()")
	class TrimTests {

		@Test
		@DisplayName("trim leading and trailing spaces")
		void trimSpaces() {
			assertEquals("hello", eval("trim(\"  hello  \")"));
		}

		@Test
		@DisplayName("trim with no extra spaces")
		void trimNoSpaces() {
			assertEquals("hello", eval("trim(\"hello\")"));
		}

		@Test
		@DisplayName("trim empty string")
		void trimEmpty() {
			assertEquals("", eval("trim(\"\")"));
		}

		@Test
		@DisplayName("trim null returns null")
		void trimNull() {
			assertNull(eval("trim(null)"));
		}

		@Test
		@DisplayName("trim only spaces becomes empty")
		void trimAllSpaces() {
			assertEquals("", eval("trim(\"   \")"));
		}
	}

	// ── upper ──

	@Nested
	@DisplayName("upper()")
	class UpperTests {

		@Test
		@DisplayName("upper of lowercase")
		void upperLower() {
			assertEquals("HELLO", eval("upper(\"hello\")"));
		}

		@Test
		@DisplayName("upper of already uppercase")
		void upperUpper() {
			assertEquals("HELLO", eval("upper(\"HELLO\")"));
		}

		@Test
		@DisplayName("upper of mixed case")
		void upperMixed() {
			assertEquals("HELLO WORLD", eval("upper(\"Hello World\")"));
		}

		@Test
		@DisplayName("upper of empty string")
		void upperEmpty() {
			assertEquals("", eval("upper(\"\")"));
		}

		@Test
		@DisplayName("upper of null returns null")
		void upperNull() {
			assertNull(eval("upper(null)"));
		}
	}

	// ── lower ──

	@Nested
	@DisplayName("lower()")
	class LowerTests {

		@Test
		@DisplayName("lower of uppercase")
		void lowerUpper() {
			assertEquals("hello", eval("lower(\"HELLO\")"));
		}

		@Test
		@DisplayName("lower of already lowercase")
		void lowerLower() {
			assertEquals("hello", eval("lower(\"hello\")"));
		}

		@Test
		@DisplayName("lower of mixed case")
		void lowerMixed() {
			assertEquals("hello world", eval("lower(\"Hello World\")"));
		}

		@Test
		@DisplayName("lower of empty string")
		void lowerEmpty() {
			assertEquals("", eval("lower(\"\")"));
		}

		@Test
		@DisplayName("lower of null returns null")
		void lowerNull() {
			assertNull(eval("lower(null)"));
		}
	}

	// ── left ──

	@Nested
	@DisplayName("left()")
	class LeftTests {

		@Test
		@DisplayName("left(str, 3) returns first 3 chars")
		void leftBasic() {
			assertEquals("hel", eval("left(\"hello\",3)"));
		}

		@Test
		@DisplayName("left(str, 0) returns empty")
		void leftZero() {
			assertEquals("", eval("left(\"hello\",0)"));
		}

		@Test
		@DisplayName("left(str, len) returns whole string")
		void leftFullLength() {
			assertEquals("hello", eval("left(\"hello\",5)"));
		}

		@Test
		@DisplayName("left with length > string length returns whole string")
		void leftExceedsLength() {
			assertEquals("hi", eval("left(\"hi\",10)"));
		}

		@Test
		@DisplayName("left of null returns null")
		void leftNull() {
			assertNull(eval("left(null,3)"));
		}
	}

	// ── right ──

	@Nested
	@DisplayName("right()")
	class RightTests {

		@Test
		@DisplayName("right(str, 3) returns last 3 chars")
		void rightBasic() {
			assertEquals("llo", eval("right(\"hello\",3)"));
		}

		@Test
		@DisplayName("right(str, 0) returns empty")
		void rightZero() {
			assertEquals("", eval("right(\"hello\",0)"));
		}

		@Test
		@DisplayName("right with length > string length returns whole string")
		void rightExceedsLength() {
			assertEquals("hi", eval("right(\"hi\",10)"));
		}

		@Test
		@DisplayName("right of null returns null")
		void rightNull() {
			assertNull(eval("right(null,3)"));
		}
	}

	// ── mid ──

	@Nested
	@DisplayName("mid()")
	class MidTests {

		@Test
		@DisplayName("mid(str, 2, 3) returns 3 chars from position 2")
		void midBasic() {
			// mid is 1-based: mid("hello", 2, 3) should return "ell"
			assertEquals("ell", eval("mid(\"hello\",2,3)"));
		}

		@Test
		@DisplayName("mid(str, 1, 5) returns whole string")
		void midFullString() {
			assertEquals("hello", eval("mid(\"hello\",1,5)"));
		}

		@Test
		@DisplayName("mid of null returns null")
		void midNull() {
			assertNull(eval("mid(null,1,3)"));
		}

		@Test
		@DisplayName("mid from end of string")
		void midFromEnd() {
			assertEquals("lo", eval("mid(\"hello\",4,2)"));
		}
	}

	// ── len ──

	@Nested
	@DisplayName("len()")
	class LenTests {

		@Test
		@DisplayName("len of normal string")
		void lenNormal() {
			assertEquals(5, ((Number) eval("len(\"hello\")")).intValue());
		}

		@Test
		@DisplayName("len of empty string")
		void lenEmpty() {
			assertEquals(0, ((Number) eval("len(\"\")")).intValue());
		}

		@Test
		@DisplayName("len of null returns null or 0")
		void lenNull() {
			Object result = eval("len(null)");
			// len(null) may return null or 0
			assertTrue(result == null || ((Number) result).intValue() == 0);
		}

		@Test
		@DisplayName("len of string with spaces")
		void lenWithSpaces() {
			assertEquals(11, ((Number) eval("len(\"hello world\")")).intValue());
		}

		@Test
		@DisplayName("len of single char")
		void lenSingleChar() {
			assertEquals(1, ((Number) eval("len(\"x\")")).intValue());
		}
	}

	// ── replace ──

	@Nested
	@DisplayName("replace()")
	class ReplaceTests {

		@Test
		@DisplayName("replace basic substring")
		void replaceBasic() {
			assertEquals("hxllo", eval("replace(\"hello\",2:1,\"x\")"));
		}

		@Test
		@DisplayName("replace with longer string")
		void replaceLonger() {
			assertEquals("hxxxllo", eval("replace(\"hello\",2:1,\"xxx\")"));
		}

		@Test
		@DisplayName("replace of null returns null")
		void replaceNull() {
			assertNull(eval("replace(null,1:1,\"x\")"));
		}
	}

	// ── Combined string expressions ──

	@Nested
	@DisplayName("Combined String Expressions")
	class CombinedTests {

		@Test
		@DisplayName("upper(left(str, 3))")
		void upperOfLeft() {
			assertEquals("HEL", eval("upper(left(\"hello\",3))"));
		}

		@Test
		@DisplayName("len(trim(str))")
		void lenOfTrim() {
			assertEquals(5, ((Number) eval("len(trim(\"  hello  \"))")).intValue());
		}

		@Test
		@DisplayName("lower(upper(str)) returns original lowercase")
		void lowerOfUpper() {
			assertEquals("hello", eval("lower(upper(\"hello\"))"));
		}

		@Test
		@DisplayName("len(left(str, 3)) = 3")
		void lenOfLeft() {
			assertEquals(3, ((Number) eval("len(left(\"hello\",3))")).intValue());
		}
	}
}
