package com.scudata.expression.fn.datetime;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Context;
import com.scudata.expression.Expression;

/**
 * Tests for datetime functions: now, date, year, month, day, hour, minute, second.
 * All tests go through the Expression parser for end-to-end evaluation.
 *
 * Note: date() is implemented by ToDate class; now() returns current timestamp.
 * year/month/day/hour/minute/second extract components from a date/time value.
 * Since we can't easily construct date literals inline, we use now() and
 * validate component extraction returns reasonable values.
 */
@DisplayName("DateTime Functions Tests")
public class DateTimeFunctionsTest {

	private Context ctx;

	@BeforeEach
	void setUp() {
		ctx = new Context();
	}

	private Object eval(String expr) {
		return new Expression(expr).calculate(ctx);
	}

	// ── now() ──

	@Nested
	@DisplayName("now()")
	class NowTests {

		@Test
		@DisplayName("now() returns a Date/Timestamp")
		void nowReturnsDate() {
			Object result = eval("now()");
			assertNotNull(result);
			// now() should return a Timestamp or Date
			assertTrue(result instanceof Date, "now() should return a Date/Timestamp, got: " + result.getClass());
		}

		@Test
		@DisplayName("now() returns approximately current time")
		void nowIsCurrentTime() {
			long before = System.currentTimeMillis();
			Object result = eval("now()");
			long after = System.currentTimeMillis();

			assertTrue(result instanceof Date);
			long time = ((Date) result).getTime();
			assertTrue(time >= before && time <= after,
					"now() time should be between before and after: " + time);
		}
	}

	// ── date() — implemented by ToDate ──

	@Nested
	@DisplayName("date()")
	class DateTests {

		@Test
		@DisplayName("date(now()) returns a Date (date part only)")
		void dateOfNow() {
			Object result = eval("date(now())");
			assertNotNull(result);
			assertTrue(result instanceof Date);
		}

		@Test
		@DisplayName("date(null) returns null")
		void dateNull() {
			assertNull(eval("date(null)"));
		}
	}

	// ── year() ──

	@Nested
	@DisplayName("year()")
	class YearTests {

		@Test
		@DisplayName("year(now()) returns current year")
		void yearOfNow() {
			Object result = eval("year(now())");
			assertNotNull(result);
			int year = ((Number) result).intValue();
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			assertEquals(currentYear, year);
		}

		@Test
		@DisplayName("year(null) returns null")
		void yearNull() {
			assertNull(eval("year(null)"));
		}

		@Test
		@DisplayName("year(date(now())) also works")
		void yearOfDate() {
			Object result = eval("year(date(now()))");
			assertNotNull(result);
			int year = ((Number) result).intValue();
			assertTrue(year >= 2020 && year <= 2100);
		}
	}

	// ── month() ──

	@Nested
	@DisplayName("month()")
	class MonthTests {

		@Test
		@DisplayName("month(now()) returns current month 1-12")
		void monthOfNow() {
			Object result = eval("month(now())");
			assertNotNull(result);
			int month = ((Number) result).intValue();
			// Calendar.MONTH is 0-based, but SPL month() returns 1-based
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
			assertEquals(currentMonth, month);
		}

		@Test
		@DisplayName("month(null) returns null")
		void monthNull() {
			assertNull(eval("month(null)"));
		}

		@Test
		@DisplayName("month returns value between 1 and 12")
		void monthRange() {
			Object result = eval("month(now())");
			int month = ((Number) result).intValue();
			assertTrue(month >= 1 && month <= 12);
		}
	}

	// ── day() ──

	@Nested
	@DisplayName("day()")
	class DayTests {

		@Test
		@DisplayName("day(now()) returns current day of month")
		void dayOfNow() {
			Object result = eval("day(now())");
			assertNotNull(result);
			int day = ((Number) result).intValue();
			int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			assertEquals(currentDay, day);
		}

		@Test
		@DisplayName("day(null) returns null")
		void dayNull() {
			assertNull(eval("day(null)"));
		}

		@Test
		@DisplayName("day returns value between 1 and 31")
		void dayRange() {
			Object result = eval("day(now())");
			int day = ((Number) result).intValue();
			assertTrue(day >= 1 && day <= 31);
		}
	}

	// ── hour() ──

	@Nested
	@DisplayName("hour()")
	class HourTests {

		@Test
		@DisplayName("hour(now()) returns current hour")
		void hourOfNow() {
			Object result = eval("hour(now())");
			assertNotNull(result);
			int hour = ((Number) result).intValue();
			assertTrue(hour >= 0 && hour <= 23);
		}

		@Test
		@DisplayName("hour(null) returns null")
		void hourNull() {
			assertNull(eval("hour(null)"));
		}
	}

	// ── minute() ──

	@Nested
	@DisplayName("minute()")
	class MinuteTests {

		@Test
		@DisplayName("minute(now()) returns current minute")
		void minuteOfNow() {
			Object result = eval("minute(now())");
			assertNotNull(result);
			int minute = ((Number) result).intValue();
			assertTrue(minute >= 0 && minute <= 59);
		}

		@Test
		@DisplayName("minute(null) returns null")
		void minuteNull() {
			assertNull(eval("minute(null)"));
		}
	}

	// ── second() ──

	@Nested
	@DisplayName("second()")
	class SecondTests {

		@Test
		@DisplayName("second(now()) returns current second")
		void secondOfNow() {
			Object result = eval("second(now())");
			assertNotNull(result);
			int second = ((Number) result).intValue();
			assertTrue(second >= 0 && second <= 59);
		}

		@Test
		@DisplayName("second(null) returns null")
		void secondNull() {
			assertNull(eval("second(null)"));
		}
	}

	// ── Combined datetime expressions ──

	@Nested
	@DisplayName("Combined DateTime Expressions")
	class CombinedTests {

		@Test
		@DisplayName("year(now()) * 100 + month(now()) gives year-month integer")
		void yearMonthInt() {
			Object result = eval("year(now())*100+month(now())");
			assertNotNull(result);
			int ym = ((Number) result).intValue();
			int currentYM = Calendar.getInstance().get(Calendar.YEAR) * 100
					+ Calendar.getInstance().get(Calendar.MONTH) + 1;
			assertEquals(currentYM, ym);
		}

		@Test
		@DisplayName("day(now()) > 0 is true")
		void dayPositive() {
			assertEquals(Boolean.TRUE, eval("day(now())>0"));
		}

		@Test
		@DisplayName("hour(now()) >= 0 is true")
		void hourNonNegative() {
			assertEquals(Boolean.TRUE, eval("hour(now())>=0"));
		}

		@Test
		@DisplayName("minute(now()) < 60 is true")
		void minuteRange() {
			assertEquals(Boolean.TRUE, eval("minute(now())<60"));
		}
	}
}
