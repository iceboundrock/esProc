package com.esproc.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scudata.dm.Sequence;

/**
 * Tests for JDBCUtil static utility methods.
 * Focuses on pure logic methods that don't require database connections
 * or complex external state.
 */
@DisplayName("JDBCUtil Tests")
public class JDBCUtilTest {

    // ========== trimSql() ==========
    @Nested
    @DisplayName("trimSql()")
    class TrimSqlTests {

        @Test
        @DisplayName("trims leading and trailing whitespace")
        void trimsWhitespace() {
            assertEquals("select 1", JDBCUtil.trimSql("  select 1  "));
        }

        @Test
        @DisplayName("removes curly braces wrapper")
        void removesCurlyBraces() {
            assertEquals("call test()", JDBCUtil.trimSql("{call test()}"));
        }

        @Test
        @DisplayName("removes curly braces with internal whitespace")
        void removesCurlyBracesWithWhitespace() {
            assertEquals("call test()", JDBCUtil.trimSql("{ call test() }"));
        }

        @Test
        @DisplayName("returns null for null input")
        void returnsNullForNull() {
            assertNull(JDBCUtil.trimSql(null));
        }

        @Test
        @DisplayName("returns null for empty string")
        void returnsNullForEmpty() {
            assertNull(JDBCUtil.trimSql(""));
        }

        @Test
        @DisplayName("returns null for whitespace-only string")
        void returnsNullForWhitespace() {
            assertNull(JDBCUtil.trimSql("   "));
        }

        @Test
        @DisplayName("preserves sql without braces")
        void preservesNoBraces() {
            assertEquals("=1+2", JDBCUtil.trimSql("=1+2"));
        }

        @Test
        @DisplayName("does not remove mismatched braces")
        void mismatchedBraces() {
            // Only starts with { but doesn't end with } — no stripping
            String result = JDBCUtil.trimSql("{call test()");
            assertEquals("{call test()", result);
        }
    }

    // ========== getTypeName() ==========
    @Nested
    @DisplayName("getTypeName()")
    class GetTypeNameTests {

        @Test
        @DisplayName("INTEGER type returns \"INTEGER\"")
        void integerType() {
            assertEquals("INTEGER", JDBCUtil.getTypeName(Types.INTEGER));
        }

        @Test
        @DisplayName("SMALLINT type returns \"SMALLINT\"")
        void smallintType() {
            assertEquals("SMALLINT", JDBCUtil.getTypeName(Types.SMALLINT));
        }

        @Test
        @DisplayName("BIGINT type returns \"BIGINT\"")
        void bigintType() {
            assertEquals("BIGINT", JDBCUtil.getTypeName(Types.BIGINT));
        }

        @Test
        @DisplayName("FLOAT type returns \"FLOAT\"")
        void floatType() {
            assertEquals("FLOAT", JDBCUtil.getTypeName(Types.FLOAT));
        }

        @Test
        @DisplayName("DOUBLE type returns \"DOUBLE\"")
        void doubleType() {
            assertEquals("DOUBLE", JDBCUtil.getTypeName(Types.DOUBLE));
        }

        @Test
        @DisplayName("DECIMAL type returns \"DECIMAL\"")
        void decimalType() {
            assertEquals("DECIMAL", JDBCUtil.getTypeName(Types.DECIMAL));
        }

        @Test
        @DisplayName("DATE type returns \"DATE\"")
        void dateType() {
            assertEquals("DATE", JDBCUtil.getTypeName(Types.DATE));
        }

        @Test
        @DisplayName("TIME type returns \"TIME\"")
        void timeType() {
            assertEquals("TIME", JDBCUtil.getTypeName(Types.TIME));
        }

        @Test
        @DisplayName("TIMESTAMP type returns \"TIMESTAMP\"")
        void timestampType() {
            assertEquals("TIMESTAMP", JDBCUtil.getTypeName(Types.TIMESTAMP));
        }

        @Test
        @DisplayName("VARCHAR type returns \"VARCHAR\"")
        void varcharType() {
            assertEquals("VARCHAR", JDBCUtil.getTypeName(Types.VARCHAR));
        }

        @Test
        @DisplayName("BOOLEAN type returns \"BOOLEAN\"")
        void booleanType() {
            assertEquals("BOOLEAN", JDBCUtil.getTypeName(Types.BOOLEAN));
        }

        @Test
        @DisplayName("BINARY type returns \"BINARY\"")
        void binaryType() {
            assertEquals("BINARY", JDBCUtil.getTypeName(Types.BINARY));
        }

        @Test
        @DisplayName("unknown type returns \"JAVA_OBJECT\"")
        void unknownType() {
            assertEquals("JAVA_OBJECT", JDBCUtil.getTypeName(9999));
        }
    }

    // ========== getTypeClassName() ==========
    @Nested
    @DisplayName("getTypeClassName()")
    class GetTypeClassNameTests {

        @Test
        @DisplayName("INTEGER -> java.lang.Integer")
        void integerClass() {
            assertEquals("java.lang.Integer", JDBCUtil.getTypeClassName(Types.INTEGER));
        }

        @Test
        @DisplayName("SMALLINT -> java.lang.Short")
        void smallintClass() {
            assertEquals("java.lang.Short", JDBCUtil.getTypeClassName(Types.SMALLINT));
        }

        @Test
        @DisplayName("BIGINT -> java.lang.Long")
        void bigintClass() {
            assertEquals("java.lang.Long", JDBCUtil.getTypeClassName(Types.BIGINT));
        }

        @Test
        @DisplayName("FLOAT -> java.lang.Float")
        void floatClass() {
            assertEquals("java.lang.Float", JDBCUtil.getTypeClassName(Types.FLOAT));
        }

        @Test
        @DisplayName("DOUBLE -> java.lang.Double")
        void doubleClass() {
            assertEquals("java.lang.Double", JDBCUtil.getTypeClassName(Types.DOUBLE));
        }

        @Test
        @DisplayName("DECIMAL -> java.math.BigDecimal")
        void decimalClass() {
            assertEquals("java.math.BigDecimal", JDBCUtil.getTypeClassName(Types.DECIMAL));
        }

        @Test
        @DisplayName("DATE -> java.sql.Date")
        void dateClass() {
            assertEquals("java.sql.Date", JDBCUtil.getTypeClassName(Types.DATE));
        }

        @Test
        @DisplayName("TIME -> java.sql.Time")
        void timeClass() {
            assertEquals("java.sql.Time", JDBCUtil.getTypeClassName(Types.TIME));
        }

        @Test
        @DisplayName("TIMESTAMP -> java.sql.Timestamp")
        void timestampClass() {
            assertEquals("java.sql.Timestamp", JDBCUtil.getTypeClassName(Types.TIMESTAMP));
        }

        @Test
        @DisplayName("VARCHAR -> java.lang.String")
        void varcharClass() {
            assertEquals("java.lang.String", JDBCUtil.getTypeClassName(Types.VARCHAR));
        }

        @Test
        @DisplayName("BOOLEAN -> java.lang.Boolean")
        void booleanClass() {
            assertEquals("java.lang.Boolean", JDBCUtil.getTypeClassName(Types.BOOLEAN));
        }

        @Test
        @DisplayName("BINARY -> java.lang.Object")
        void binaryClass() {
            assertEquals("java.lang.Object", JDBCUtil.getTypeClassName(Types.BINARY));
        }

        @Test
        @DisplayName("unknown type -> java.lang.Object")
        void unknownClass() {
            assertEquals("java.lang.Object", JDBCUtil.getTypeClassName(9999));
        }
    }

    // ========== getSQLTypeByType() ==========
    @Nested
    @DisplayName("getSQLTypeByType()")
    class GetSQLTypeByTypeTests {

        @Test
        @DisplayName("DT_INT -> Types.INTEGER")
        void dtInt() {
            assertEquals(Types.INTEGER, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_INT));
        }

        @Test
        @DisplayName("DT_SHORT -> Types.SMALLINT")
        void dtShort() {
            assertEquals(Types.SMALLINT, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_SHORT));
        }

        @Test
        @DisplayName("DT_LONG -> Types.BIGINT")
        void dtLong() {
            assertEquals(Types.BIGINT, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_LONG));
        }

        @Test
        @DisplayName("DT_DOUBLE -> Types.DOUBLE")
        void dtDouble() {
            assertEquals(Types.DOUBLE, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_DOUBLE));
        }

        @Test
        @DisplayName("DT_DECIMAL -> Types.DECIMAL")
        void dtDecimal() {
            assertEquals(Types.DECIMAL, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_DECIMAL));
        }

        @Test
        @DisplayName("DT_DATE -> Types.DATE")
        void dtDate() {
            assertEquals(Types.DATE, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_DATE));
        }

        @Test
        @DisplayName("DT_TIME -> Types.TIME")
        void dtTime() {
            assertEquals(Types.TIME, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_TIME));
        }

        @Test
        @DisplayName("DT_DATETIME -> Types.TIMESTAMP")
        void dtDatetime() {
            assertEquals(Types.TIMESTAMP, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_DATETIME));
        }

        @Test
        @DisplayName("DT_STRING -> Types.VARCHAR")
        void dtString() {
            assertEquals(Types.VARCHAR, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_STRING));
        }

        @Test
        @DisplayName("DT_BOOLEAN -> Types.BOOLEAN")
        void dtBoolean() {
            assertEquals(Types.BOOLEAN, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_BOOLEAN));
        }

        @Test
        @DisplayName("DT_BYTE_SERIES -> Types.BINARY")
        void dtByteSeries() {
            assertEquals(Types.BINARY, JDBCUtil.getSQLTypeByType(com.scudata.common.Types.DT_BYTE_SERIES));
        }

        @Test
        @DisplayName("unknown type -> Types.JAVA_OBJECT")
        void unknownDtType() {
            assertEquals(Types.JAVA_OBJECT, JDBCUtil.getSQLTypeByType((byte) 127));
        }
    }

    // ========== getProperDataType() ==========
    @Nested
    @DisplayName("getProperDataType()")
    class GetProperDataTypeTests {

        @Test
        @DisplayName("Integer returns DT_INT")
        void integerObj() {
            assertEquals(com.scudata.common.Types.DT_INT, JDBCUtil.getProperDataType(42));
        }

        @Test
        @DisplayName("Long returns appropriate type")
        void longObj() {
            byte type = JDBCUtil.getProperDataType(42L);
            assertTrue(type == com.scudata.common.Types.DT_LONG || type == com.scudata.common.Types.DT_BIGINT);
        }

        @Test
        @DisplayName("Double returns appropriate type")
        void doubleObj() {
            byte type = JDBCUtil.getProperDataType(3.14);
            assertTrue(type == com.scudata.common.Types.DT_DOUBLE || type == com.scudata.common.Types.DT_FLOAT);
        }

        @Test
        @DisplayName("String returns DT_STRING")
        void stringObj() {
            assertEquals(com.scudata.common.Types.DT_STRING, JDBCUtil.getProperDataType("hello"));
        }

        @Test
        @DisplayName("Boolean returns DT_BOOLEAN")
        void booleanObj() {
            assertEquals(com.scudata.common.Types.DT_BOOLEAN, JDBCUtil.getProperDataType(true));
        }

        @Test
        @DisplayName("BigDecimal returns DT_DECIMAL")
        void bigDecimalObj() {
            assertEquals(com.scudata.common.Types.DT_DECIMAL, JDBCUtil.getProperDataType(new BigDecimal("3.14")));
        }
    }

    // ========== getType() ==========
    @Nested
    @DisplayName("getType()")
    class GetTypeTests {

        @Test
        @DisplayName("null object returns existing type")
        void nullReturnsExisting() {
            assertEquals(Types.INTEGER, JDBCUtil.getType(null, Types.INTEGER));
        }

        @Test
        @DisplayName("JAVA_OBJECT type stays as JAVA_OBJECT")
        void javaObjectStays() {
            assertEquals(Types.JAVA_OBJECT, JDBCUtil.getType(42, Types.JAVA_OBJECT));
        }

        @Test
        @DisplayName("NULL type gets replaced by object's type")
        void nullTypeReplaced() {
            int result = JDBCUtil.getType(42, Types.NULL);
            assertEquals(Types.INTEGER, result);
        }

        @Test
        @DisplayName("Integer with INTEGER type stays INTEGER")
        void integerStays() {
            assertEquals(Types.INTEGER, JDBCUtil.getType(42, Types.INTEGER));
        }

        @Test
        @DisplayName("String with NULL type becomes VARCHAR")
        void stringType() {
            assertEquals(Types.VARCHAR, JDBCUtil.getType("hello", Types.NULL));
        }
    }

    // ========== getCallExp() ==========
    @Nested
    @DisplayName("getCallExp()")
    class GetCallExpTests {

        @Test
        @DisplayName("basic call with params string")
        void basicCall() throws SQLException {
            String result = JDBCUtil.getCallExp("test.splx", "a,b", 2);
            assertEquals("jdbccall(\"test.splx\",a,b)", result);
        }

        @Test
        @DisplayName("call with no params string uses ? placeholders")
        void callNoParamsString() throws SQLException {
            String result = JDBCUtil.getCallExp("test.splx", null, 3);
            assertEquals("jdbccall(\"test.splx\",?,?,?)", result);
        }

        @Test
        @DisplayName("call with empty params string and 0 paramCount")
        void callEmptyParams() throws SQLException {
            String result = JDBCUtil.getCallExp("test.splx", "", 0);
            assertEquals("jdbccall(\"test.splx\")", result);
        }

        @Test
        @DisplayName("call with opt parameter")
        void callWithOpt() throws SQLException {
            String result = JDBCUtil.getCallExp("test.splx", "a", 1, "j");
            assertEquals("jdbccall@j(\"test.splx\",a)", result);
        }

        @Test
        @DisplayName("call with null opt parameter")
        void callWithNullOpt() throws SQLException {
            String result = JDBCUtil.getCallExp("test.splx", "a", 1, null);
            assertEquals("jdbccall(\"test.splx\",a)", result);
        }
    }

    // ========== getCallNameParam() ==========
    @Nested
    @DisplayName("getCallNameParam()")
    class GetCallNameParamTests {

        @Test
        @DisplayName("call with parentheses extracts name and params")
        void callWithParens() throws SQLException {
            String[] result = JDBCUtil.getCallNameParam("call test.splx(a,b)");
            assertEquals("test.splx", result[0]);
            assertEquals("a,b", result[1]);
        }

        @Test
        @DisplayName("call without parentheses extracts name only")
        void callWithoutParens() throws SQLException {
            String[] result = JDBCUtil.getCallNameParam("call test.splx");
            assertEquals("test.splx", result[0]);
            assertNull(result[1]);
        }

        @Test
        @DisplayName("call with quoted name strips quotes")
        void callQuotedName() throws SQLException {
            String[] result = JDBCUtil.getCallNameParam("call 'test.splx'");
            assertEquals("test.splx", result[0]);
            assertNull(result[1]);
        }

        @Test
        @DisplayName("malformed call throws SQLException")
        void malformedCall() {
            assertThrows(SQLException.class, () -> {
                JDBCUtil.getCallNameParam("call (test.splx");
            });
        }
    }

    // ========== getSplNameParam() ==========
    @Nested
    @DisplayName("getSplNameParam()")
    class GetSplNameParamTests {

        @Test
        @DisplayName("spl with parentheses - ArgumentTokenizer skips parenthesized content")
        void splWithParens() {
            String[] result = JDBCUtil.getSplNameParam("test.splx(a,b)");
            // ArgumentTokenizer with parentheses=true skips over (a,b) as parenthesized content
            assertEquals("test.splx(a,b)", result[0]);
            assertNull(result[1]);
        }

        @Test
        @DisplayName("spl without parentheses extracts name only")
        void splWithoutParens() {
            String[] result = JDBCUtil.getSplNameParam("test.splx");
            assertEquals("test.splx", result[0]);
            assertNull(result[1]);
        }

        @Test
        @DisplayName("spl with space-separated params")
        void splWithSpaceParams() {
            String[] result = JDBCUtil.getSplNameParam("test.splx param1");
            assertEquals("test.splx", result[0]);
            assertEquals("param1", result[1]);
        }
    }

    // ========== isCallsStatement() ==========
    @Nested
    @DisplayName("isCallsStatement()")
    class IsCallsStatementTests {

        @Test
        @DisplayName("calls statement returns true")
        void callsStatement() {
            assertTrue(JDBCUtil.isCallsStatement("calls test.splx(a)"));
        }

        @Test
        @DisplayName("call statement (not calls) returns false")
        void callStatement() {
            assertFalse(JDBCUtil.isCallsStatement("call test.splx(a)"));
        }

        @Test
        @DisplayName("null returns false")
        void nullInput() {
            assertFalse(JDBCUtil.isCallsStatement(null));
        }

        @Test
        @DisplayName("empty string returns false")
        void emptyInput() {
            assertFalse(JDBCUtil.isCallsStatement(""));
        }

        @Test
        @DisplayName("expression statement returns false")
        void expressionStatement() {
            assertFalse(JDBCUtil.isCallsStatement("=1+2"));
        }
    }

    // ========== prepareArg() ==========
    @Nested
    @DisplayName("prepareArg()")
    class PrepareArgTests {

        @Test
        @DisplayName("null parameters returns empty Sequence")
        void nullParams() throws SQLException {
            Sequence result = JDBCUtil.prepareArg(null);
            assertNotNull(result);
            assertEquals(0, result.length());
        }

        @Test
        @DisplayName("empty list returns empty Sequence")
        void emptyParams() throws SQLException {
            Sequence result = JDBCUtil.prepareArg(new ArrayList<>());
            assertNotNull(result);
            assertEquals(0, result.length());
        }

        @Test
        @DisplayName("list with values returns Sequence with values")
        void withValues() throws SQLException {
            List<Object> params = new ArrayList<>();
            params.add(1);
            params.add("hello");
            params.add(3.14);
            Sequence result = JDBCUtil.prepareArg(params);
            assertEquals(3, result.length());
        }

        @Test
        @DisplayName("list with null values preserves nulls")
        void withNulls() throws SQLException {
            List<Object> params = new ArrayList<>();
            params.add(null);
            params.add(42);
            Sequence result = JDBCUtil.prepareArg(params);
            assertEquals(2, result.length());
            assertNull(result.get(1));
            assertEquals(42, result.get(2));
        }
    }

    // ========== array2String() ==========
    @Nested
    @DisplayName("array2String()")
    class Array2StringTests {

        @Test
        @DisplayName("Object array to string")
        void objectArray() {
            Object[] arr = {"a", "b", "c"};
            String result = JDBCUtil.array2String(arr);
            assertTrue(result.contains("a"));
            assertTrue(result.contains("b"));
            assertTrue(result.contains("c"));
            assertTrue(result.startsWith("["));
            assertTrue(result.endsWith("]"));
        }

        @Test
        @DisplayName("null Object array returns []")
        void nullObjectArray() {
            String result = JDBCUtil.array2String((Object[]) null);
            assertEquals("[]", result);
        }

        @Test
        @DisplayName("empty Object array returns []")
        void emptyObjectArray() {
            String result = JDBCUtil.array2String(new Object[]{});
            assertEquals("[]", result);
        }

        @Test
        @DisplayName("int array to string")
        void intArray() {
            int[] arr = {1, 2, 3};
            String result = JDBCUtil.array2String(arr);
            assertTrue(result.contains("1"));
            assertTrue(result.contains("2"));
            assertTrue(result.contains("3"));
            assertTrue(result.startsWith("["));
            assertTrue(result.endsWith("]"));
        }

        @Test
        @DisplayName("null int array returns []")
        void nullIntArray() {
            String result = JDBCUtil.array2String((int[]) null);
            assertEquals("[]", result);
        }

        @Test
        @DisplayName("empty int array returns []")
        void emptyIntArray() {
            String result = JDBCUtil.array2String(new int[]{});
            assertEquals("[]", result);
        }
    }

    // ========== checkSqlLength() ==========
    @Nested
    @DisplayName("checkSqlLength()")
    class CheckSqlLengthTests {

        @Test
        @DisplayName("null sql does not throw")
        void nullSql() {
            assertDoesNotThrow(() -> JDBCUtil.checkSqlLength(null));
        }

        @Test
        @DisplayName("short sql does not throw")
        void shortSql() {
            assertDoesNotThrow(() -> JDBCUtil.checkSqlLength("select 1"));
        }

        @Test
        @DisplayName("sql at max length does not throw")
        void maxLength() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 65536; i++) {
                sb.append("x");
            }
            assertDoesNotThrow(() -> JDBCUtil.checkSqlLength(sb.toString()));
        }

        @Test
        @DisplayName("sql exceeding max length throws SQLException")
        void exceedsMaxLength() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 65537; i++) {
                sb.append("x");
            }
            assertThrows(SQLException.class, () -> JDBCUtil.checkSqlLength(sb.toString()));
        }
    }

    // ========== getJdbcSqlType() ==========
    @Nested
    @DisplayName("getJdbcSqlType()")
    class GetJdbcSqlTypeTests {

        @Test
        @DisplayName("null returns TYPE_NONE")
        void nullSql() {
            assertEquals(JDBCConsts.TYPE_NONE, JDBCUtil.getJdbcSqlType(null));
        }

        @Test
        @DisplayName("empty string returns TYPE_NONE")
        void emptySql() {
            assertEquals(JDBCConsts.TYPE_NONE, JDBCUtil.getJdbcSqlType(""));
        }

        @Test
        @DisplayName("> prefix returns TYPE_EXE")
        void executeStatement() {
            assertEquals(JDBCConsts.TYPE_EXE, JDBCUtil.getJdbcSqlType(">output 1"));
        }

        @Test
        @DisplayName("= prefix returns TYPE_EXP")
        void expressionStatement() {
            assertEquals(JDBCConsts.TYPE_EXP, JDBCUtil.getJdbcSqlType("=1+2"));
        }

        @Test
        @DisplayName("call prefix returns TYPE_CALL")
        void callStatement() {
            assertEquals(JDBCConsts.TYPE_CALL, JDBCUtil.getJdbcSqlType("call test.splx()"));
        }

        @Test
        @DisplayName("calls prefix returns TYPE_CALLS")
        void callsStatement() {
            assertEquals(JDBCConsts.TYPE_CALLS, JDBCUtil.getJdbcSqlType("calls test.splx()"));
        }

        @Test
        @DisplayName("{call ...} unwraps braces then detects TYPE_CALL")
        void bracedCallStatement() {
            assertEquals(JDBCConsts.TYPE_CALL, JDBCUtil.getJdbcSqlType("{call test.splx()}"));
        }
    }

    // ========== Static flag tests ==========
    @Nested
    @DisplayName("Static flags")
    class StaticFlagTests {

        @Test
        @DisplayName("isDebugMode defaults to false")
        void debugModeDefault() {
            // Check that the flag exists and is accessible
            assertFalse(JDBCUtil.isDebugMode);
        }

        @Test
        @DisplayName("isCompatiblesql defaults to false")
        void compatibleSqlDefault() {
            assertFalse(JDBCUtil.isCompatiblesql);
        }
    }
}
