package com.esproc.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link com.esproc.jdbc.ResultSetMetaData}.
 *
 * Tests both constructors:
 *   - (String[] names, int[] types): custom column metadata
 *   - (byte type): predefined metadata for JDBC catalog queries
 *
 * The properties bitmask is 0x00100110 for all columns. In binary that has
 * bits 4, 8, and 20 set, which map to:
 *   CURRENCY (constant 4, 1<<4=bit4)   -> true
 *   DEFINITELY_WRITABLE (constant 8, 1<<8=bit8) -> true
 *   Bit 20 is unused by any accessor method.
 *
 * All other property bits (AUTO_INCREMENT=1, CASE_SENSITIVE=2, SEARCHABLE=3,
 * SIGNED=5, READ_ONLY=6, WRITABLE=7) are NOT set -> false.
 */
@DisplayName("ResultSetMetaData Tests")
class ResultSetMetaDataTest {

	// =========================================================================
	// Tests using (String[] names, int[] types) constructor
	// =========================================================================

	@Nested
	@DisplayName("Constructor with (String[], int[])")
	class StringArrayConstructorTests {

		private ResultSetMetaData rsmd;
		private String[] names;
		private int[] types;

		@BeforeEach
		void setUp() throws SQLException {
			names = new String[] { "ID", "NAME", "AMOUNT", "CREATED" };
			types = new int[] { Types.INTEGER, Types.VARCHAR, Types.DOUBLE, Types.TIMESTAMP };
			rsmd = new ResultSetMetaData(names, types);
		}

		@Test
		@DisplayName("getColumnCount returns correct count")
		void testGetColumnCount() throws SQLException {
			assertEquals(4, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("getColumnCount with single column")
		void testGetColumnCountSingle() throws SQLException {
			ResultSetMetaData single = new ResultSetMetaData(
					new String[] { "X" }, new int[] { Types.VARCHAR });
			assertEquals(1, single.getColumnCount());
		}

		@Test
		@DisplayName("getColumnName returns correct names (1-based)")
		void testGetColumnName() throws SQLException {
			assertEquals("ID", rsmd.getColumnName(1));
			assertEquals("NAME", rsmd.getColumnName(2));
			assertEquals("AMOUNT", rsmd.getColumnName(3));
			assertEquals("CREATED", rsmd.getColumnName(4));
		}

		@Test
		@DisplayName("getColumnName returns null for out-of-range column")
		void testGetColumnNameOutOfRange() throws SQLException {
			assertNull(rsmd.getColumnName(5));
			assertNull(rsmd.getColumnName(100));
		}

		@Test
		@DisplayName("getColumnLabel returns same as name by default")
		void testGetColumnLabel() throws SQLException {
			assertEquals("ID", rsmd.getColumnLabel(1));
			assertEquals("NAME", rsmd.getColumnLabel(2));
			assertEquals("AMOUNT", rsmd.getColumnLabel(3));
			assertEquals("CREATED", rsmd.getColumnLabel(4));
		}

		@Test
		@DisplayName("getColumnLabel returns null for out-of-range column")
		void testGetColumnLabelOutOfRange() throws SQLException {
			assertNull(rsmd.getColumnLabel(5));
		}

		@Test
		@DisplayName("getColumnType returns correct SQL types")
		void testGetColumnType() throws SQLException {
			assertEquals(Types.INTEGER, rsmd.getColumnType(1));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(2));
			assertEquals(Types.DOUBLE, rsmd.getColumnType(3));
			assertEquals(Types.TIMESTAMP, rsmd.getColumnType(4));
		}

		@Test
		@DisplayName("getColumnType returns VARCHAR for out-of-range column")
		void testGetColumnTypeOutOfRange() throws SQLException {
			assertEquals(Types.VARCHAR, rsmd.getColumnType(5));
		}

		@Test
		@DisplayName("getColumnType substitutes VARCHAR for type 0")
		void testGetColumnTypeZeroSubstitution() throws SQLException {
			ResultSetMetaData m = new ResultSetMetaData(
					new String[] { "COL" }, new int[] { 0 });
			assertEquals(Types.VARCHAR, m.getColumnType(1));
		}

		@Test
		@DisplayName("getColumnTypeName returns JDBCUtil type name mapping")
		void testGetColumnTypeName() throws SQLException {
			// Populated by JDBCUtil.getTypeName(type) in initColumnProperties
			assertNotNull(rsmd.getColumnTypeName(1)); // INTEGER
			assertNotNull(rsmd.getColumnTypeName(2)); // VARCHAR
		}

		@Test
		@DisplayName("getColumnTypeName returns null for out-of-range column")
		void testGetColumnTypeNameOutOfRange() throws SQLException {
			assertNull(rsmd.getColumnTypeName(5));
		}

		@Test
		@DisplayName("getColumnClassName returns JDBCUtil class name mapping")
		void testGetColumnClassName() throws SQLException {
			assertNotNull(rsmd.getColumnClassName(1)); // INTEGER
			assertNotNull(rsmd.getColumnClassName(2)); // VARCHAR
		}

		@Test
		@DisplayName("getColumnClassName returns null for out-of-range column")
		void testGetColumnClassNameOutOfRange() throws SQLException {
			assertNull(rsmd.getColumnClassName(5));
		}

		// --- Property bitmask tests (0x00100110) ---
		// Bits set: 4 (CURRENCY), 8 (DEFINITELY_WRITABLE), 20 (unused)
		// Bits NOT set: 1 (AUTO_INCREMENT), 2 (CASE_SENSITIVE), 3 (SEARCHABLE),
		//               5 (SIGNED), 6 (READ_ONLY), 7 (WRITABLE)

		@Test
		@DisplayName("isAutoIncrement returns false (bit 1 not set in 0x00100110)")
		void testIsAutoIncrement() throws SQLException {
			assertFalse(rsmd.isAutoIncrement(1));
			assertFalse(rsmd.isAutoIncrement(2));
			assertFalse(rsmd.isAutoIncrement(3));
			assertFalse(rsmd.isAutoIncrement(4));
		}

		@Test
		@DisplayName("isCaseSensitive returns false (bit 2 not set in 0x00100110)")
		void testIsCaseSensitive() throws SQLException {
			assertFalse(rsmd.isCaseSensitive(1));
			assertFalse(rsmd.isCaseSensitive(2));
		}

		@Test
		@DisplayName("isSearchable returns false (bit 3 not set in 0x00100110)")
		void testIsSearchable() throws SQLException {
			assertFalse(rsmd.isSearchable(1));
			assertFalse(rsmd.isSearchable(2));
		}

		@Test
		@DisplayName("isCurrency returns true (bit 4 IS set in 0x00100110)")
		void testIsCurrency() throws SQLException {
			assertTrue(rsmd.isCurrency(1));
			assertTrue(rsmd.isCurrency(2));
			assertTrue(rsmd.isCurrency(3));
			assertTrue(rsmd.isCurrency(4));
		}

		@Test
		@DisplayName("isSigned returns false (bit 5 not set in 0x00100110)")
		void testIsSigned() throws SQLException {
			assertFalse(rsmd.isSigned(1));
			assertFalse(rsmd.isSigned(2));
		}

		@Test
		@DisplayName("isReadOnly returns false (bit 6 not set in 0x00100110)")
		void testIsReadOnly() throws SQLException {
			assertFalse(rsmd.isReadOnly(1));
			assertFalse(rsmd.isReadOnly(2));
		}

		@Test
		@DisplayName("isWritable returns false (bit 7 not set in 0x00100110)")
		void testIsWritable() throws SQLException {
			assertFalse(rsmd.isWritable(1));
			assertFalse(rsmd.isWritable(2));
		}

		@Test
		@DisplayName("isDefinitelyWritable returns true (bit 8 IS set in 0x00100110)")
		void testIsDefinitelyWritable() throws SQLException {
			assertTrue(rsmd.isDefinitelyWritable(1));
			assertTrue(rsmd.isDefinitelyWritable(2));
			assertTrue(rsmd.isDefinitelyWritable(3));
			assertTrue(rsmd.isDefinitelyWritable(4));
		}

		@Test
		@DisplayName("isNullable returns columnNullable for all columns")
		void testIsNullable() throws SQLException {
			for (int i = 1; i <= 4; i++) {
				assertEquals(java.sql.ResultSetMetaData.columnNullable,
						rsmd.isNullable(i));
			}
		}

		@Test
		@DisplayName("getColumnDisplaySize always returns 10000")
		void testGetColumnDisplaySize() throws SQLException {
			assertEquals(10000, rsmd.getColumnDisplaySize(1));
			assertEquals(10000, rsmd.getColumnDisplaySize(2));
			assertEquals(10000, rsmd.getColumnDisplaySize(3));
			assertEquals(10000, rsmd.getColumnDisplaySize(4));
		}

		@Test
		@DisplayName("getPrecision returns MAX_VALUE for VARCHAR, 0 for others")
		void testGetPrecision() throws SQLException {
			// INTEGER -> 0
			assertEquals(0, rsmd.getPrecision(1));
			// VARCHAR -> Integer.MAX_VALUE
			assertEquals(Integer.MAX_VALUE, rsmd.getPrecision(2));
			// DOUBLE -> 0
			assertEquals(0, rsmd.getPrecision(3));
			// TIMESTAMP -> 0
			assertEquals(0, rsmd.getPrecision(4));
		}

		@Test
		@DisplayName("getPrecision returns 0 for out-of-range column")
		void testGetPrecisionOutOfRange() throws SQLException {
			assertEquals(0, rsmd.getPrecision(5));
		}

		@Test
		@DisplayName("getScale returns 0 for all columns")
		void testGetScale() throws SQLException {
			assertEquals(0, rsmd.getScale(1));
			assertEquals(0, rsmd.getScale(2));
			assertEquals(0, rsmd.getScale(3));
			assertEquals(0, rsmd.getScale(4));
		}

		@Test
		@DisplayName("getScale returns 0 for out-of-range column")
		void testGetScaleOutOfRange() throws SQLException {
			assertEquals(0, rsmd.getScale(5));
		}

		@Test
		@DisplayName("getSchemaName always returns empty string")
		void testGetSchemaName() throws SQLException {
			assertEquals("", rsmd.getSchemaName(1));
			assertEquals("", rsmd.getSchemaName(2));
			assertEquals("", rsmd.getSchemaName(100));
		}

		@Test
		@DisplayName("getTableName returns null (no table names populated)")
		void testGetTableName() throws SQLException {
			// tableNames list is created with capacity but no elements added
			assertNull(rsmd.getTableName(1));
		}

		@Test
		@DisplayName("getCatalogName returns null (no catalog names populated)")
		void testGetCatalogName() throws SQLException {
			// catalogNames list is created with capacity but no elements added
			assertNull(rsmd.getCatalogName(1));
		}

		@Test
		@DisplayName("isWrapperFor always returns false")
		void testIsWrapperFor() throws SQLException {
			assertFalse(rsmd.isWrapperFor(java.sql.ResultSetMetaData.class));
			assertFalse(rsmd.isWrapperFor(ResultSetMetaData.class));
			assertFalse(rsmd.isWrapperFor(Object.class));
		}

		@Test
		@DisplayName("unwrap always returns null")
		void testUnwrap() throws SQLException {
			assertNull(rsmd.unwrap(java.sql.ResultSetMetaData.class));
			assertNull(rsmd.unwrap(Object.class));
		}
	}

	// =========================================================================
	// Tests with all-VARCHAR columns (precision should be MAX_VALUE for all)
	// =========================================================================

	@Nested
	@DisplayName("All VARCHAR columns")
	class AllVarcharTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() throws SQLException {
			rsmd = new ResultSetMetaData(
					new String[] { "A", "B", "C" },
					new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR });
		}

		@Test
		@DisplayName("All VARCHAR columns have MAX_VALUE precision")
		void testAllVarcharPrecision() throws SQLException {
			assertEquals(Integer.MAX_VALUE, rsmd.getPrecision(1));
			assertEquals(Integer.MAX_VALUE, rsmd.getPrecision(2));
			assertEquals(Integer.MAX_VALUE, rsmd.getPrecision(3));
		}

		@Test
		@DisplayName("getColumnType returns VARCHAR for all")
		void testAllVarcharTypes() throws SQLException {
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(2));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(3));
		}
	}

	// =========================================================================
	// Tests with all numeric types
	// =========================================================================

	@Nested
	@DisplayName("Numeric column types")
	class NumericTypeTests {

		@Test
		@DisplayName("All numeric types have precision 0")
		void testNumericPrecision() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					new String[] { "INT_COL", "BIGINT_COL", "DOUBLE_COL", "DECIMAL_COL" },
					new int[] { Types.INTEGER, Types.BIGINT, Types.DOUBLE, Types.DECIMAL });
			assertEquals(0, rsmd.getPrecision(1));
			assertEquals(0, rsmd.getPrecision(2));
			assertEquals(0, rsmd.getPrecision(3));
			assertEquals(0, rsmd.getPrecision(4));
		}
	}

	// =========================================================================
	// Tests using (byte type) constructor with predefined metadata types
	// =========================================================================

	@Nested
	@DisplayName("Constructor with (byte type) - GET_EMPTY_RESULT")
	class EmptyResultTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_EMPTY_RESULT);
		}

		@Test
		@DisplayName("GET_EMPTY_RESULT has 1 column named NORESULT")
		void testEmptyResultColumnCount() throws SQLException {
			assertEquals(1, rsmd.getColumnCount());
			assertEquals("NORESULT", rsmd.getColumnName(1));
		}

		@Test
		@DisplayName("GET_EMPTY_RESULT column type is VARCHAR")
		void testEmptyResultColumnType() throws SQLException {
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
		}

		@Test
		@DisplayName("GET_EMPTY_RESULT column label matches name")
		void testEmptyResultColumnLabel() throws SQLException {
			assertEquals("NORESULT", rsmd.getColumnLabel(1));
		}

		@Test
		@DisplayName("GET_EMPTY_RESULT column has VARCHAR precision")
		void testEmptyResultPrecision() throws SQLException {
			assertEquals(Integer.MAX_VALUE, rsmd.getPrecision(1));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_PROCEDURES")
	class ProceduresTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_PROCEDURES);
		}

		@Test
		@DisplayName("GET_PROCEDURES has 9 columns")
		void testProceduresColumnCount() throws SQLException {
			assertEquals(9, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_PROCEDURES first column is PROCEDURE_CAT")
		void testProceduresFirstColumn() throws SQLException {
			assertEquals("PROCEDURE_CAT", rsmd.getColumnName(1));
		}

		@Test
		@DisplayName("GET_PROCEDURES third column is PROCEDURE_NAME")
		void testProceduresThirdColumn() throws SQLException {
			assertEquals("PROCEDURE_NAME", rsmd.getColumnName(3));
		}

		@Test
		@DisplayName("GET_PROCEDURES eighth column PROCEDURE_TYPE is INTEGER")
		void testProceduresTypeColumn() throws SQLException {
			assertEquals(Types.INTEGER, rsmd.getColumnType(8));
		}

		@Test
		@DisplayName("GET_PROCEDURES last column is SPECIFIC_NAME")
		void testProceduresLastColumn() throws SQLException {
			assertEquals("SPECIFIC_NAME", rsmd.getColumnName(9));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(9));
		}

		@Test
		@DisplayName("GET_PROCEDURES most columns are VARCHAR")
		void testProceduresMostColumnsVarchar() throws SQLException {
			for (int i = 1; i <= 7; i++) {
				assertEquals(Types.VARCHAR, rsmd.getColumnType(i),
						"Column " + i + " should be VARCHAR");
			}
			// Column 9 is also VARCHAR
			assertEquals(Types.VARCHAR, rsmd.getColumnType(9));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_SCHEMAS")
	class SchemasTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_SCHEMAS);
		}

		@Test
		@DisplayName("GET_SCHEMAS has 2 columns")
		void testSchemasColumnCount() throws SQLException {
			assertEquals(2, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_SCHEMAS columns are TABLE_SCHEM and TABLE_CATALOG")
		void testSchemasColumnNames() throws SQLException {
			assertEquals("TABLE_SCHEM", rsmd.getColumnName(1));
			assertEquals("TABLE_CATALOG", rsmd.getColumnName(2));
		}

		@Test
		@DisplayName("GET_SCHEMAS both columns are VARCHAR")
		void testSchemasColumnTypes() throws SQLException {
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(2));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_TABLES")
	class TablesTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_TABLES);
		}

		@Test
		@DisplayName("GET_TABLES has 10 columns")
		void testTablesColumnCount() throws SQLException {
			assertEquals(10, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_TABLES first three columns correct")
		void testTablesFirstColumns() throws SQLException {
			assertEquals("TABLE_CAT", rsmd.getColumnName(1));
			assertEquals("TABLE_SCHEM", rsmd.getColumnName(2));
			assertEquals("TABLE_NAME", rsmd.getColumnName(3));
		}

		@Test
		@DisplayName("GET_TABLES all columns are VARCHAR")
		void testTablesAllVarchar() throws SQLException {
			for (int i = 1; i <= 10; i++) {
				assertEquals(Types.VARCHAR, rsmd.getColumnType(i),
						"Column " + i + " should be VARCHAR");
			}
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_COLUMNS")
	class ColumnsTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_COLUMNS);
		}

		@Test
		@DisplayName("GET_COLUMNS has 24 columns")
		void testColumnsColumnCount() throws SQLException {
			assertEquals(24, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_COLUMNS first column is TABLE_CAT")
		void testColumnsFirstColumn() throws SQLException {
			assertEquals("TABLE_CAT", rsmd.getColumnName(1));
		}

		@Test
		@DisplayName("GET_COLUMNS DATA_TYPE column (5th) is INTEGER")
		void testColumnsDataTypeColumn() throws SQLException {
			assertEquals("DATA_TYPE", rsmd.getColumnName(5));
			assertEquals(Types.INTEGER, rsmd.getColumnType(5));
		}

		@Test
		@DisplayName("GET_COLUMNS last two columns are autoincrement and generated")
		void testColumnsLastColumns() throws SQLException {
			assertEquals("IS_AUTOINCREMENT", rsmd.getColumnName(23));
			assertEquals("IS_GENERATEDCOLUMN", rsmd.getColumnName(24));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_CATALOGS")
	class CatalogsTests {

		@Test
		@DisplayName("GET_CATALOGS has 1 column named TABLE_CAT")
		void testCatalogsMetadata() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_CATALOGS);
			assertEquals(1, rsmd.getColumnCount());
			assertEquals("TABLE_CAT", rsmd.getColumnName(1));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_TABLE_TYPES")
	class TableTypesTests {

		@Test
		@DisplayName("GET_TABLE_TYPES has 1 column named TABLE_TYPE")
		void testTableTypesMetadata() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_TABLE_TYPES);
			assertEquals(1, rsmd.getColumnCount());
			assertEquals("TABLE_TYPE", rsmd.getColumnName(1));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_IMPORTED_KEYS")
	class ImportedKeysTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_IMPORTED_KEYS);
		}

		@Test
		@DisplayName("GET_IMPORTED_KEYS has 14 columns")
		void testImportedKeysColumnCount() throws SQLException {
			assertEquals(14, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_IMPORTED_KEYS starts with PKTABLE_CAT")
		void testImportedKeysFirstColumn() throws SQLException {
			assertEquals("PKTABLE_CAT", rsmd.getColumnName(1));
		}

		@Test
		@DisplayName("GET_IMPORTED_KEYS KEY_SEQ (9th) is INTEGER")
		void testImportedKeysKeySeqType() throws SQLException {
			assertEquals("KEY_SEQ", rsmd.getColumnName(9));
			assertEquals(Types.INTEGER, rsmd.getColumnType(9));
		}

		@Test
		@DisplayName("GET_IMPORTED_KEYS last column is DEFERRABILITY")
		void testImportedKeysLastColumn() throws SQLException {
			assertEquals("DEFERRABILITY", rsmd.getColumnName(14));
			assertEquals(Types.INTEGER, rsmd.getColumnType(14));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_EXPORTED_KEYS")
	class ExportedKeysTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_EXPORTED_KEYS);
		}

		@Test
		@DisplayName("GET_EXPORTED_KEYS has 14 columns (same structure as imported)")
		void testExportedKeysColumnCount() throws SQLException {
			assertEquals(14, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_EXPORTED_KEYS starts with PKTABLE_CAT")
		void testExportedKeysFirstColumn() throws SQLException {
			assertEquals("PKTABLE_CAT", rsmd.getColumnName(1));
		}

		@Test
		@DisplayName("GET_EXPORTED_KEYS has same column names as imported keys")
		void testExportedKeysSameAsImported() throws SQLException {
			ResultSetMetaData imported = new ResultSetMetaData(
					(byte) ResultSet.GET_IMPORTED_KEYS);
			for (int i = 1; i <= 14; i++) {
				assertEquals(imported.getColumnName(i), rsmd.getColumnName(i),
						"Column " + i + " name should match");
				assertEquals(imported.getColumnType(i), rsmd.getColumnType(i),
						"Column " + i + " type should match");
			}
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_PRIMARY_KEYS")
	class PrimaryKeysTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_PRIMARY_KEYS);
		}

		@Test
		@DisplayName("GET_PRIMARY_KEYS has 6 columns")
		void testPrimaryKeysColumnCount() throws SQLException {
			assertEquals(6, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_PRIMARY_KEYS column names correct")
		void testPrimaryKeysColumnNames() throws SQLException {
			assertEquals("TABLE_CAT", rsmd.getColumnName(1));
			assertEquals("TABLE_SCHEM", rsmd.getColumnName(2));
			assertEquals("TABLE_NAME", rsmd.getColumnName(3));
			assertEquals("COLUMN_NAME", rsmd.getColumnName(4));
			assertEquals("KEY_SEQ", rsmd.getColumnName(5));
			assertEquals("PK_NAME", rsmd.getColumnName(6));
		}

		@Test
		@DisplayName("GET_PRIMARY_KEYS KEY_SEQ is INTEGER, rest VARCHAR")
		void testPrimaryKeysColumnTypes() throws SQLException {
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(2));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(3));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(4));
			assertEquals(Types.INTEGER, rsmd.getColumnType(5));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(6));
		}
	}

	@Nested
	@DisplayName("Constructor with (byte type) - GET_PROCEDURE_COLUMNS")
	class ProcedureColumnsTests {

		private ResultSetMetaData rsmd;

		@BeforeEach
		void setUp() {
			rsmd = new ResultSetMetaData((byte) ResultSet.GET_PROCEDURE_COLUMNS);
		}

		@Test
		@DisplayName("GET_PROCEDURE_COLUMNS has 20 columns")
		void testProcedureColumnsCount() throws SQLException {
			assertEquals(20, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("GET_PROCEDURE_COLUMNS first column is PROCEDURE_CAT")
		void testProcedureColumnsFirstColumn() throws SQLException {
			assertEquals("PROCEDURE_CAT", rsmd.getColumnName(1));
		}

		@Test
		@DisplayName("GET_PROCEDURE_COLUMNS COLUMN_TYPE (5th) is INTEGER")
		void testProcedureColumnsType() throws SQLException {
			assertEquals("COLUMN_TYPE", rsmd.getColumnName(5));
			assertEquals(Types.INTEGER, rsmd.getColumnType(5));
		}

		@Test
		@DisplayName("GET_PROCEDURE_COLUMNS last column is SPECIFIC_NAME (VARCHAR)")
		void testProcedureColumnsLastColumn() throws SQLException {
			assertEquals("SPECIFIC_NAME", rsmd.getColumnName(20));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(20));
		}
	}

	// =========================================================================
	// Common property tests across byte-type constructed instances
	// =========================================================================

	@Nested
	@DisplayName("Common properties for byte-type constructed metadata")
	class ByteTypeCommonPropertyTests {

		@Test
		@DisplayName("Properties bitmask applied to byte-type constructed instances")
		void testPropertiesOnByteType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_TABLES);

			// Same bitmask 0x00100110 applied via initColumnProperties
			assertFalse(rsmd.isAutoIncrement(1));
			assertFalse(rsmd.isCaseSensitive(1));
			assertFalse(rsmd.isSearchable(1));
			assertTrue(rsmd.isCurrency(1));
			assertFalse(rsmd.isSigned(1));
			assertFalse(rsmd.isReadOnly(1));
			assertFalse(rsmd.isWritable(1));
			assertTrue(rsmd.isDefinitelyWritable(1));
		}

		@Test
		@DisplayName("isNullable returns columnNullable for byte-type instances")
		void testNullableOnByteType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_SCHEMAS);
			assertEquals(java.sql.ResultSetMetaData.columnNullable,
					rsmd.isNullable(1));
			assertEquals(java.sql.ResultSetMetaData.columnNullable,
					rsmd.isNullable(2));
		}

		@Test
		@DisplayName("getColumnDisplaySize returns 10000 for byte-type instances")
		void testDisplaySizeOnByteType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_PRIMARY_KEYS);
			assertEquals(10000, rsmd.getColumnDisplaySize(1));
			assertEquals(10000, rsmd.getColumnDisplaySize(6));
		}

		@Test
		@DisplayName("getSchemaName returns empty string for byte-type instances")
		void testSchemaNameOnByteType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_CATALOGS);
			assertEquals("", rsmd.getSchemaName(1));
		}

		@Test
		@DisplayName("getScale returns 0 for byte-type instances")
		void testScaleOnByteType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_PROCEDURES);
			assertEquals(0, rsmd.getScale(1));
			assertEquals(0, rsmd.getScale(9));
		}

		@Test
		@DisplayName("VARCHAR columns in byte-type have MAX_VALUE precision")
		void testVarcharPrecisionOnByteType() throws SQLException {
			// GET_CATALOGS: single VARCHAR column
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_CATALOGS);
			assertEquals(Integer.MAX_VALUE, rsmd.getPrecision(1));
		}

		@Test
		@DisplayName("INTEGER columns in byte-type have 0 precision")
		void testIntegerPrecisionOnByteType() throws SQLException {
			// GET_PROCEDURES: column 8 (PROCEDURE_TYPE) is INTEGER
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_PROCEDURES);
			assertEquals(0, rsmd.getPrecision(8));
		}

		@Test
		@DisplayName("isWrapperFor returns false for byte-type instances")
		void testIsWrapperForOnByteType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_EMPTY_RESULT);
			assertFalse(rsmd.isWrapperFor(java.sql.ResultSetMetaData.class));
		}

		@Test
		@DisplayName("unwrap returns null for byte-type instances")
		void testUnwrapOnByteType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					(byte) ResultSet.GET_EMPTY_RESULT);
			assertNull(rsmd.unwrap(java.sql.ResultSetMetaData.class));
		}
	}

	// =========================================================================
	// Default constructor tests
	// =========================================================================

	@Nested
	@DisplayName("Default no-arg constructor")
	class DefaultConstructorTests {

		@Test
		@DisplayName("Default constructor sets colCount to 0")
		void testDefaultConstructorColumnCount() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertEquals(0, rsmd.getColumnCount());
		}

		@Test
		@DisplayName("Default constructor: getColumnName returns null")
		void testDefaultConstructorColumnNameNull() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertNull(rsmd.getColumnName(1));
		}

		@Test
		@DisplayName("Default constructor: getColumnLabel returns null")
		void testDefaultConstructorColumnLabelNull() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertNull(rsmd.getColumnLabel(1));
		}

		@Test
		@DisplayName("Default constructor: getColumnType returns VARCHAR")
		void testDefaultConstructorColumnType() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			// columnTypes is null, so falls through to return VARCHAR
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
		}

		@Test
		@DisplayName("Default constructor: getColumnTypeName returns null")
		void testDefaultConstructorTypeName() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertNull(rsmd.getColumnTypeName(1));
		}

		@Test
		@DisplayName("Default constructor: getColumnClassName returns null")
		void testDefaultConstructorClassName() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertNull(rsmd.getColumnClassName(1));
		}

		@Test
		@DisplayName("Default constructor: getPrecision returns 0")
		void testDefaultConstructorPrecision() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertEquals(0, rsmd.getPrecision(1));
		}

		@Test
		@DisplayName("Default constructor: getScale returns 0")
		void testDefaultConstructorScale() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertEquals(0, rsmd.getScale(1));
		}

		@Test
		@DisplayName("Default constructor: getTableName returns null")
		void testDefaultConstructorTableName() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertNull(rsmd.getTableName(1));
		}

		@Test
		@DisplayName("Default constructor: getCatalogName returns null")
		void testDefaultConstructorCatalogName() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertNull(rsmd.getCatalogName(1));
		}

		@Test
		@DisplayName("Default constructor: getSchemaName returns empty string")
		void testDefaultConstructorSchemaName() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData();
			assertEquals("", rsmd.getSchemaName(1));
		}
	}

	// =========================================================================
	// Edge case tests
	// =========================================================================

	@Nested
	@DisplayName("Edge cases")
	class EdgeCaseTests {

		@Test
		@DisplayName("Mixed SQL types get correct type names and class names")
		void testMixedSqlTypes() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					new String[] { "A", "B", "C", "D", "E" },
					new int[] { Types.BIT, Types.BIGINT, Types.FLOAT,
							Types.DATE, Types.BLOB });

			assertEquals(5, rsmd.getColumnCount());
			assertEquals(Types.BIT, rsmd.getColumnType(1));
			assertEquals(Types.BIGINT, rsmd.getColumnType(2));
			assertEquals(Types.FLOAT, rsmd.getColumnType(3));
			assertEquals(Types.DATE, rsmd.getColumnType(4));
			assertEquals(Types.BLOB, rsmd.getColumnType(5));

			// Non-VARCHAR types should have precision 0
			assertEquals(0, rsmd.getPrecision(1));
			assertEquals(0, rsmd.getPrecision(2));
			assertEquals(0, rsmd.getPrecision(3));
			assertEquals(0, rsmd.getPrecision(4));
			assertEquals(0, rsmd.getPrecision(5));
		}

		@Test
		@DisplayName("Single column metadata")
		void testSingleColumn() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					new String[] { "ONLY" }, new int[] { Types.INTEGER });
			assertEquals(1, rsmd.getColumnCount());
			assertEquals("ONLY", rsmd.getColumnName(1));
			assertEquals("ONLY", rsmd.getColumnLabel(1));
			assertEquals(Types.INTEGER, rsmd.getColumnType(1));
		}

		@Test
		@DisplayName("Many columns metadata")
		void testManyColumns() throws SQLException {
			int n = 50;
			String[] names = new String[n];
			int[] types = new int[n];
			for (int i = 0; i < n; i++) {
				names[i] = "COL_" + i;
				types[i] = (i % 2 == 0) ? Types.VARCHAR : Types.INTEGER;
			}
			ResultSetMetaData rsmd = new ResultSetMetaData(names, types);
			assertEquals(n, rsmd.getColumnCount());
			assertEquals("COL_0", rsmd.getColumnName(1));
			assertEquals("COL_49", rsmd.getColumnName(50));
			assertEquals(Types.VARCHAR, rsmd.getColumnType(1));
			assertEquals(Types.INTEGER, rsmd.getColumnType(50));
		}

		@Test
		@DisplayName("Column names with special characters")
		void testSpecialCharColumnNames() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					new String[] { "col with space", "col.dot", "col-dash", "" },
					new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR });
			assertEquals("col with space", rsmd.getColumnName(1));
			assertEquals("col.dot", rsmd.getColumnName(2));
			assertEquals("col-dash", rsmd.getColumnName(3));
			assertEquals("", rsmd.getColumnName(4));
		}

		@Test
		@DisplayName("Implements java.sql.ResultSetMetaData interface")
		void testImplementsInterface() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					new String[] { "X" }, new int[] { Types.VARCHAR });
			assertTrue(rsmd instanceof java.sql.ResultSetMetaData);
		}

		@Test
		@DisplayName("Implements Externalizable interface")
		void testImplementsExternalizable() throws SQLException {
			ResultSetMetaData rsmd = new ResultSetMetaData(
					new String[] { "X" }, new int[] { Types.VARCHAR });
			assertTrue(rsmd instanceof java.io.Externalizable);
		}

		@Test
		@DisplayName("All byte-type constants produce valid metadata")
		void testAllByteTypeConstants() throws SQLException {
			byte[] typeConstants = {
					ResultSet.GET_EMPTY_RESULT,   // 0
					ResultSet.GET_PROCEDURES,     // 1
					ResultSet.GET_PROCEDURE_COLUMNS, // 2
					ResultSet.GET_SCHEMAS,        // 3
					ResultSet.GET_TABLES,         // 4
					ResultSet.GET_COLUMNS,        // 5
					ResultSet.GET_CATALOGS,       // 6
					ResultSet.GET_TABLE_TYPES,    // 7
					ResultSet.GET_IMPORTED_KEYS,  // 8
					ResultSet.GET_EXPORTED_KEYS,  // 9
					ResultSet.GET_PRIMARY_KEYS    // 10
			};
			int[] expectedCounts = { 1, 9, 20, 2, 10, 24, 1, 1, 14, 14, 6 };

			for (int i = 0; i < typeConstants.length; i++) {
				ResultSetMetaData rsmd = new ResultSetMetaData(typeConstants[i]);
				assertEquals(expectedCounts[i], rsmd.getColumnCount(),
						"Type " + typeConstants[i] + " should have "
								+ expectedCounts[i] + " columns");
				// Every type should have at least one column name
				assertNotNull(rsmd.getColumnName(1),
						"Type " + typeConstants[i] + " first column should have a name");
			}
		}
	}

	// =========================================================================
	// Serialization round-trip test
	// =========================================================================

	@Nested
	@DisplayName("Serialization (Externalizable)")
	class SerializationTests {

		@Test
		@DisplayName("Round-trip serialization preserves metadata")
		void testSerializationRoundTrip() throws Exception {
			String[] names = { "ID", "NAME", "VALUE" };
			int[] types = { Types.INTEGER, Types.VARCHAR, Types.DOUBLE };
			ResultSetMetaData original = new ResultSetMetaData(names, types);

			// Serialize
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
			original.writeExternal(oos);
			oos.flush();

			// Deserialize
			java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(
					baos.toByteArray());
			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
			ResultSetMetaData restored = new ResultSetMetaData();
			restored.readExternal(ois);

			// Verify
			assertEquals(original.getColumnCount(), restored.getColumnCount());
			for (int i = 1; i <= 3; i++) {
				assertEquals(original.getColumnName(i), restored.getColumnName(i));
				assertEquals(original.getColumnLabel(i), restored.getColumnLabel(i));
				assertEquals(original.getColumnType(i), restored.getColumnType(i));
				assertEquals(original.getColumnTypeName(i), restored.getColumnTypeName(i));
				assertEquals(original.getColumnClassName(i), restored.getColumnClassName(i));
				assertEquals(original.getColumnDisplaySize(i), restored.getColumnDisplaySize(i));
				assertEquals(original.getPrecision(i), restored.getPrecision(i));
				assertEquals(original.getScale(i), restored.getScale(i));
				assertEquals(original.isNullable(i), restored.isNullable(i));
				assertEquals(original.isAutoIncrement(i), restored.isAutoIncrement(i));
				assertEquals(original.isCaseSensitive(i), restored.isCaseSensitive(i));
				assertEquals(original.isSearchable(i), restored.isSearchable(i));
				assertEquals(original.isCurrency(i), restored.isCurrency(i));
				assertEquals(original.isSigned(i), restored.isSigned(i));
				assertEquals(original.isReadOnly(i), restored.isReadOnly(i));
				assertEquals(original.isWritable(i), restored.isWritable(i));
				assertEquals(original.isDefinitelyWritable(i),
						restored.isDefinitelyWritable(i));
			}
		}

		@Test
		@DisplayName("Round-trip serialization for byte-type constructed metadata")
		void testSerializationByteType() throws Exception {
			ResultSetMetaData original = new ResultSetMetaData(
					(byte) ResultSet.GET_PRIMARY_KEYS);

			// Serialize
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
			original.writeExternal(oos);
			oos.flush();

			// Deserialize
			java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(
					baos.toByteArray());
			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
			ResultSetMetaData restored = new ResultSetMetaData();
			restored.readExternal(ois);

			assertEquals(6, restored.getColumnCount());
			assertEquals("TABLE_CAT", restored.getColumnName(1));
			assertEquals("PK_NAME", restored.getColumnName(6));
			assertEquals(Types.INTEGER, restored.getColumnType(5));
		}
	}
}
