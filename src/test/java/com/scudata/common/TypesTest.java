package com.scudata.common;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Types} — data type constants, conversion, and classification utilities.
 */
@DisplayName("Types")
class TypesTest {

    // ---------------------------------------------------------------
    // Constants verification
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Type constants")
    class ConstantsTests {

        @Test
        @DisplayName("Scalar type constants have expected byte values")
        void scalarConstants() {
            assertEquals((byte) 0, Types.DT_DEFAULT);
            assertEquals((byte) 1, Types.DT_INT);
            assertEquals((byte) 2, Types.DT_LONG);
            assertEquals((byte) 3, Types.DT_SHORT);
            assertEquals((byte) 4, Types.DT_BIGINT);
            assertEquals((byte) 5, Types.DT_FLOAT);
            assertEquals((byte) 6, Types.DT_DOUBLE);
            assertEquals((byte) 7, Types.DT_DECIMAL);
            assertEquals((byte) 8, Types.DT_DATE);
            assertEquals((byte) 9, Types.DT_TIME);
            assertEquals((byte) 10, Types.DT_DATETIME);
            assertEquals((byte) 11, Types.DT_STRING);
            assertEquals((byte) 12, Types.DT_BOOLEAN);
        }

        @Test
        @DisplayName("Series type constants have expected byte values")
        void seriesConstants() {
            assertEquals((byte) 51, Types.DT_INT_SERIES);
            assertEquals((byte) 52, Types.DT_LONG_SERIES);
            assertEquals((byte) 53, Types.DT_SHORT_SERIES);
            assertEquals((byte) 54, Types.DT_BIGINT_SERIES);
            assertEquals((byte) 55, Types.DT_FLOAT_SERIES);
            assertEquals((byte) 56, Types.DT_DOUBLE_SERIES);
            assertEquals((byte) 57, Types.DT_DECIMAL_SERIES);
            assertEquals((byte) 58, Types.DT_DATE_SERIES);
            assertEquals((byte) 59, Types.DT_TIME_SERIES);
            assertEquals((byte) 60, Types.DT_DATETIME_SERIES);
            assertEquals((byte) 61, Types.DT_STRING_SERIES);
            assertEquals((byte) 62, Types.DT_BYTE_SERIES);
        }

        @Test
        @DisplayName("Special type constants have expected byte values")
        void specialConstants() {
            assertEquals((byte) 101, Types.DT_CURSOR);
            assertEquals((byte) 102, Types.DT_AUTOINCREMENT);
            assertEquals((byte) 103, Types.DT_SERIALBYTES);
        }
    }

    // ---------------------------------------------------------------
    // getProperData — 2-arg overload delegates to 3-arg
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("getProperData(type, val)")
    class GetProperDataTests {

        @Test
        @DisplayName("Returns null for null input")
        void nullInput() throws Exception {
            assertNull(Types.getProperData(Types.DT_INT, null));
        }

        @Test
        @DisplayName("Returns null for empty string (non-string type)")
        void emptyStringNonStringType() throws Exception {
            assertNull(Types.getProperData(Types.DT_INT, ""));
            assertNull(Types.getProperData(Types.DT_INT, "   "));
        }

        @Test
        @DisplayName("DT_STRING returns original value (untrimmed)")
        void stringType() throws Exception {
            assertEquals("  hello  ", Types.getProperData(Types.DT_STRING, "  hello  "));
        }

        @Test
        @DisplayName("DT_STRING returns null for empty when ignoreString is false")
        void stringTypeEmpty() throws Exception {
            // 2-arg overload passes ignoreString=false
            assertNull(Types.getProperData(Types.DT_STRING, ""));
        }

        @Test
        @DisplayName("DT_STRING with ignoreString=true returns empty string")
        void stringTypeIgnore() throws Exception {
            assertEquals("", Types.getProperData(Types.DT_STRING, "", true));
        }

        @Test
        @DisplayName("DT_INT parses integer")
        void intType() throws Exception {
            assertEquals(Integer.valueOf(42), Types.getProperData(Types.DT_INT, "42"));
            assertEquals(Integer.valueOf(-7), Types.getProperData(Types.DT_INT, " -7 "));
        }

        @Test
        @DisplayName("DT_DOUBLE parses double")
        void doubleType() throws Exception {
            assertEquals(Double.valueOf(3.14), Types.getProperData(Types.DT_DOUBLE, "3.14"));
        }

        @Test
        @DisplayName("DT_LONG parses long")
        void longType() throws Exception {
            assertEquals(Long.valueOf(123456789L), Types.getProperData(Types.DT_LONG, "123456789"));
        }

        @Test
        @DisplayName("DT_SHORT parses short")
        void shortType() throws Exception {
            assertEquals(Short.valueOf((short) 32), Types.getProperData(Types.DT_SHORT, "32"));
        }

        @Test
        @DisplayName("DT_BIGINT parses BigInteger")
        void bigIntType() throws Exception {
            Object result = Types.getProperData(Types.DT_BIGINT, "99999999999999999999");
            assertInstanceOf(BigInteger.class, result);
            assertEquals(new BigInteger("99999999999999999999"), result);
        }

        @Test
        @DisplayName("DT_FLOAT parses float")
        void floatType() throws Exception {
            assertEquals(Float.valueOf(1.5f), Types.getProperData(Types.DT_FLOAT, "1.5"));
        }

        @Test
        @DisplayName("DT_DECIMAL parses BigDecimal")
        void decimalType() throws Exception {
            Object result = Types.getProperData(Types.DT_DECIMAL, "123.456");
            assertInstanceOf(BigDecimal.class, result);
            assertEquals(new BigDecimal("123.456"), result);
        }

        @Test
        @DisplayName("DT_BOOLEAN parses true/false (case-insensitive)")
        void booleanType() throws Exception {
            assertEquals(Boolean.TRUE, Types.getProperData(Types.DT_BOOLEAN, "true"));
            assertEquals(Boolean.TRUE, Types.getProperData(Types.DT_BOOLEAN, " TRUE "));
            assertEquals(Boolean.FALSE, Types.getProperData(Types.DT_BOOLEAN, "false"));
            assertEquals(Boolean.FALSE, Types.getProperData(Types.DT_BOOLEAN, " False "));
            assertNull(Types.getProperData(Types.DT_BOOLEAN, "yes"));
        }

        @Test
        @DisplayName("Unknown type returns original string")
        void unknownType() throws Exception {
            Object result = Types.getProperData((byte) 127, "hello");
            assertEquals("hello", result);
        }
    }

    // ---------------------------------------------------------------
    // getProperDataType
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("getProperDataType")
    class GetProperDataTypeTests {

        @Test
        @DisplayName("String returns DT_STRING")
        void stringValue() {
            assertEquals(Types.DT_STRING, Types.getProperDataType("hello"));
        }

        @Test
        @DisplayName("Double/Float returns DT_DOUBLE")
        void doubleFloatValue() {
            assertEquals(Types.DT_DOUBLE, Types.getProperDataType(3.14));
            assertEquals(Types.DT_DOUBLE, Types.getProperDataType(1.0f));
        }

        @Test
        @DisplayName("Integer/Long/BigInteger returns DT_LONG")
        void intLongBigIntValue() {
            assertEquals(Types.DT_LONG, Types.getProperDataType(42));
            assertEquals(Types.DT_LONG, Types.getProperDataType(100L));
            assertEquals(Types.DT_LONG, Types.getProperDataType(BigInteger.TEN));
        }

        @Test
        @DisplayName("sql.Time returns DT_TIME")
        void timeValue() {
            assertEquals(Types.DT_TIME, Types.getProperDataType(new Time(0)));
        }

        @Test
        @DisplayName("sql.Timestamp returns DT_DATETIME")
        void timestampValue() {
            assertEquals(Types.DT_DATETIME, Types.getProperDataType(new Timestamp(0)));
        }

        @Test
        @DisplayName("sql.Date returns DT_DATE")
        void dateValue() {
            assertEquals(Types.DT_DATE, Types.getProperDataType(new Date(0)));
        }

        @Test
        @DisplayName("BigDecimal returns DT_DECIMAL")
        void decimalValue() {
            assertEquals(Types.DT_DECIMAL, Types.getProperDataType(new BigDecimal("1.0")));
        }

        @Test
        @DisplayName("Boolean returns DT_BOOLEAN")
        void booleanValue() {
            assertEquals(Types.DT_BOOLEAN, Types.getProperDataType(Boolean.TRUE));
        }

        @Test
        @DisplayName("Unrecognized type defaults to DT_STRING")
        void unknownValue() {
            assertEquals(Types.DT_STRING, Types.getProperDataType(new Object()));
        }
    }

    // ---------------------------------------------------------------
    // getTypeBySQLType
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("getTypeBySQLType")
    class GetTypeBySQLTypeTests {

        @Test
        @DisplayName("Maps standard SQL types correctly")
        void standardMappings() {
            assertEquals(Types.DT_INT, Types.getTypeBySQLType(java.sql.Types.INTEGER));
            assertEquals(Types.DT_SHORT, Types.getTypeBySQLType(java.sql.Types.SMALLINT));
            assertEquals(Types.DT_SHORT, Types.getTypeBySQLType(java.sql.Types.TINYINT));
            assertEquals(Types.DT_BIGINT, Types.getTypeBySQLType(java.sql.Types.BIGINT));
            assertEquals(Types.DT_FLOAT, Types.getTypeBySQLType(java.sql.Types.FLOAT));
            assertEquals(Types.DT_DOUBLE, Types.getTypeBySQLType(java.sql.Types.DOUBLE));
            assertEquals(Types.DT_DOUBLE, Types.getTypeBySQLType(java.sql.Types.REAL));
            assertEquals(Types.DT_DECIMAL, Types.getTypeBySQLType(java.sql.Types.DECIMAL));
            assertEquals(Types.DT_DECIMAL, Types.getTypeBySQLType(java.sql.Types.NUMERIC));
            assertEquals(Types.DT_DATE, Types.getTypeBySQLType(java.sql.Types.DATE));
            assertEquals(Types.DT_TIME, Types.getTypeBySQLType(java.sql.Types.TIME));
            assertEquals(Types.DT_DATETIME, Types.getTypeBySQLType(java.sql.Types.TIMESTAMP));
            assertEquals(Types.DT_STRING, Types.getTypeBySQLType(java.sql.Types.CHAR));
            assertEquals(Types.DT_STRING, Types.getTypeBySQLType(java.sql.Types.VARCHAR));
            assertEquals(Types.DT_STRING, Types.getTypeBySQLType(java.sql.Types.LONGVARCHAR));
            assertEquals(Types.DT_BOOLEAN, Types.getTypeBySQLType(java.sql.Types.BOOLEAN));
        }

        @Test
        @DisplayName("Binary types map to DT_BYTE_SERIES")
        void binaryMappings() {
            assertEquals(Types.DT_BYTE_SERIES, Types.getTypeBySQLType(java.sql.Types.BINARY));
            assertEquals(Types.DT_BYTE_SERIES, Types.getTypeBySQLType(java.sql.Types.BLOB));
            assertEquals(Types.DT_BYTE_SERIES, Types.getTypeBySQLType(java.sql.Types.VARBINARY));
            assertEquals(Types.DT_BYTE_SERIES, Types.getTypeBySQLType(java.sql.Types.LONGVARBINARY));
        }

        @Test
        @DisplayName("Unknown SQL type returns DT_DEFAULT")
        void unknownSqlType() {
            assertEquals(Types.DT_DEFAULT, Types.getTypeBySQLType(9999));
        }
    }

    // ---------------------------------------------------------------
    // isNumberType / isDateType
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("isNumberType and isDateType")
    class ClassificationTests {

        @Test
        @DisplayName("isNumberType returns true for DT_INT through DT_DECIMAL")
        void numberTypes() {
            assertTrue(Types.isNumberType(Types.DT_INT));
            assertTrue(Types.isNumberType(Types.DT_LONG));
            assertTrue(Types.isNumberType(Types.DT_SHORT));
            assertTrue(Types.isNumberType(Types.DT_BIGINT));
            assertTrue(Types.isNumberType(Types.DT_FLOAT));
            assertTrue(Types.isNumberType(Types.DT_DOUBLE));
            assertTrue(Types.isNumberType(Types.DT_DECIMAL));
        }

        @Test
        @DisplayName("isNumberType returns false for non-number types")
        void notNumberTypes() {
            assertFalse(Types.isNumberType(Types.DT_DEFAULT));
            assertFalse(Types.isNumberType(Types.DT_DATE));
            assertFalse(Types.isNumberType(Types.DT_STRING));
            assertFalse(Types.isNumberType(Types.DT_BOOLEAN));
        }

        @Test
        @DisplayName("isDateType returns true for DT_DATE, DT_TIME, DT_DATETIME")
        void dateTypes() {
            assertTrue(Types.isDateType(Types.DT_DATE));
            assertTrue(Types.isDateType(Types.DT_TIME));
            assertTrue(Types.isDateType(Types.DT_DATETIME));
        }

        @Test
        @DisplayName("isDateType returns false for non-date types")
        void notDateTypes() {
            assertFalse(Types.isDateType(Types.DT_DEFAULT));
            assertFalse(Types.isDateType(Types.DT_INT));
            assertFalse(Types.isDateType(Types.DT_STRING));
            assertFalse(Types.isDateType(Types.DT_DECIMAL));
        }
    }
}
