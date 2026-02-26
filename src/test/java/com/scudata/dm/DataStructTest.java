package com.scudata.dm;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.scudata.common.RQException;

@DisplayName("Tests for DataStruct")
class DataStructTest {

	private DataStruct ds;

	@BeforeEach
	void setUp() {
		ds = new DataStruct(new String[]{"id", "name", "age"});
	}

	// ===== Constructor tests =====

	@Test
	@DisplayName("Constructor with valid field names stores them correctly")
	void testConstructorWithValidFields() {
		assertEquals(3, ds.getFieldCount());
		assertEquals("id", ds.getFieldName(0));
		assertEquals("name", ds.getFieldName(1));
		assertEquals("age", ds.getFieldName(2));
	}

	@Test
	@DisplayName("Constructor with null fields throws RQException")
	void testConstructorWithNullFieldsThrows() {
		assertThrows(RQException.class, () -> new DataStruct(null));
	}

	@Test
	@DisplayName("Constructor with empty array creates struct with zero fields")
	void testConstructorWithEmptyArray() {
		DataStruct empty = new DataStruct(new String[0]);
		assertEquals(0, empty.getFieldCount());
	}

	@Test
	@DisplayName("Constructor auto-renames null or empty field names to _n")
	void testConstructorAutoRenamesBlankFields() {
		DataStruct auto = new DataStruct(new String[]{null, "", "valid"});
		assertEquals("_1", auto.getFieldName(0));
		assertEquals("_2", auto.getFieldName(1));
		assertEquals("valid", auto.getFieldName(2));
	}

	@Test
	@DisplayName("No-arg constructor exists for serialization")
	void testNoArgConstructor() {
		DataStruct empty = new DataStruct();
		assertNotNull(empty);
	}

	// ===== getFieldIndex tests =====

	@Test
	@DisplayName("getFieldIndex returns correct index for existing field")
	void testGetFieldIndexFound() {
		assertEquals(0, ds.getFieldIndex("id"));
		assertEquals(1, ds.getFieldIndex("name"));
		assertEquals(2, ds.getFieldIndex("age"));
	}

	@Test
	@DisplayName("getFieldIndex returns -1 for non-existent field")
	void testGetFieldIndexNotFound() {
		assertEquals(-1, ds.getFieldIndex("nonexistent"));
	}

	@Test
	@DisplayName("getFieldIndex returns -1 for null or empty fieldName")
	void testGetFieldIndexNullOrEmpty() {
		assertEquals(-1, ds.getFieldIndex(null));
		assertEquals(-1, ds.getFieldIndex(""));
	}

	@Test
	@DisplayName("getFieldIndex supports #n syntax (1-based)")
	void testGetFieldIndexHashSyntax() {
		assertEquals(0, ds.getFieldIndex("#1"));
		assertEquals(1, ds.getFieldIndex("#2"));
		assertEquals(2, ds.getFieldIndex("#3"));
	}

	@Test
	@DisplayName("getFieldIndex returns -1 for #n out of range")
	void testGetFieldIndexHashSyntaxOutOfRange() {
		assertEquals(-1, ds.getFieldIndex("#0"));
		assertEquals(-1, ds.getFieldIndex("#4"));
	}

	@Test
	@DisplayName("getFieldIndex returns -1 for invalid #n formats")
	void testGetFieldIndexHashSyntaxInvalid() {
		// "#" alone is length 1, isFieldId requires length >= 2
		assertEquals(-1, ds.getFieldIndex("#"));
		// "#abc" has non-digit chars after #
		assertEquals(-1, ds.getFieldIndex("#abc"));
	}

	// ===== getFieldName tests =====

	@Test
	@DisplayName("getFieldName throws RQException for negative index")
	void testGetFieldNameNegativeIndex() {
		assertThrows(RQException.class, () -> ds.getFieldName(-1));
	}

	@Test
	@DisplayName("getFieldName throws RQException for index >= fieldCount")
	void testGetFieldNameIndexTooLarge() {
		assertThrows(RQException.class, () -> ds.getFieldName(3));
	}

	// ===== getFieldCount tests =====

	@Test
	@DisplayName("getFieldCount returns number of fields")
	void testGetFieldCount() {
		assertEquals(3, ds.getFieldCount());
		assertEquals(1, new DataStruct(new String[]{"only"}).getFieldCount());
	}

	// ===== isCompatible(DataStruct) tests =====

	@Test
	@DisplayName("isCompatible returns true for same reference")
	void testIsCompatibleSameReference() {
		assertTrue(ds.isCompatible(ds));
	}

	@Test
	@DisplayName("isCompatible returns false for null")
	void testIsCompatibleNull() {
		assertFalse(ds.isCompatible((DataStruct) null));
	}

	@Test
	@DisplayName("isCompatible returns true for matching fields")
	void testIsCompatibleMatchingFields() {
		DataStruct other = new DataStruct(new String[]{"id", "name", "age"});
		assertTrue(ds.isCompatible(other));
	}

	@Test
	@DisplayName("isCompatible returns false when field count differs")
	void testIsCompatibleDifferentCount() {
		DataStruct other = new DataStruct(new String[]{"id", "name"});
		assertFalse(ds.isCompatible(other));
	}

	@Test
	@DisplayName("isCompatible returns false when field names differ")
	void testIsCompatibleDifferentNames() {
		DataStruct other = new DataStruct(new String[]{"id", "name", "salary"});
		assertFalse(ds.isCompatible(other));
	}

	@Test
	@DisplayName("isCompatible treats null and empty names as equivalent")
	void testIsCompatibleNullEmptyEquivalent() {
		// Constructor will auto-rename null/empty to _n, so we use setFieldName
		// to inject raw nulls for this test
		DataStruct a = new DataStruct(new String[]{"x"});
		a.setFieldName(new String[]{null});

		DataStruct b = new DataStruct(new String[]{"y"});
		b.setFieldName(new String[]{""});

		assertTrue(a.isCompatible(b));
		assertTrue(b.isCompatible(a));
	}

	@Test
	@DisplayName("isCompatible returns false when one field is null/empty and other is not")
	void testIsCompatibleNullVsNonEmpty() {
		DataStruct a = new DataStruct(new String[]{"x"});
		a.setFieldName(new String[]{null});

		DataStruct b = new DataStruct(new String[]{"realName"});
		assertFalse(a.isCompatible(b));
	}

	// ===== isCompatible(String[]) tests =====

	@Test
	@DisplayName("isCompatible(String[]) returns true for matching names")
	void testIsCompatibleStringArrayMatch() {
		assertTrue(ds.isCompatible(new String[]{"id", "name", "age"}));
	}

	@Test
	@DisplayName("isCompatible(String[]) returns false for different length")
	void testIsCompatibleStringArrayDifferentLength() {
		assertFalse(ds.isCompatible(new String[]{"id", "name"}));
	}

	@Test
	@DisplayName("isCompatible(String[]) returns false for different names")
	void testIsCompatibleStringArrayDifferentNames() {
		assertFalse(ds.isCompatible(new String[]{"id", "name", "salary"}));
	}

	// ===== setPrimary / getPrimary / getPKIndex / getPKCount tests =====

	@Test
	@DisplayName("setPrimary with valid key sets primary and pkIndex")
	void testSetPrimaryValid() {
		ds.setPrimary(new String[]{"id"});
		assertArrayEquals(new String[]{"id"}, ds.getPrimary());
		assertArrayEquals(new int[]{0}, ds.getPKIndex());
		assertEquals(1, ds.getPKCount());
	}

	@Test
	@DisplayName("setPrimary with multiple keys")
	void testSetPrimaryMultipleKeys() {
		ds.setPrimary(new String[]{"id", "name"});
		assertArrayEquals(new String[]{"id", "name"}, ds.getPrimary());
		assertArrayEquals(new int[]{0, 1}, ds.getPKIndex());
		assertEquals(2, ds.getPKCount());
	}

	@Test
	@DisplayName("setPrimary with null clears primary key")
	void testSetPrimaryNullClears() {
		ds.setPrimary(new String[]{"id"});
		ds.setPrimary(null);
		assertNull(ds.getPrimary());
		assertNull(ds.getPKIndex());
		assertEquals(0, ds.getPKCount());
	}

	@Test
	@DisplayName("setPrimary with empty array clears primary key")
	void testSetPrimaryEmptyClears() {
		ds.setPrimary(new String[]{"id"});
		ds.setPrimary(new String[0]);
		assertNull(ds.getPrimary());
		assertNull(ds.getPKIndex());
		assertEquals(0, ds.getPKCount());
	}

	@Test
	@DisplayName("setPrimary throws RQException for non-existent key field")
	void testSetPrimaryNonExistentField() {
		assertThrows(RQException.class, () -> ds.setPrimary(new String[]{"bogus"}));
	}

	// ===== setPrimary with opt / isSeqKey / getTimeKeyCount =====

	@Test
	@DisplayName("setPrimary with opt 't' sets time key")
	void testSetPrimaryTimeKey() {
		ds.setPrimary(new String[]{"id", "age"}, "t");
		assertEquals(1, ds.getTimeKeyCount());
		assertFalse(ds.isSeqKey());
	}

	@Test
	@DisplayName("setPrimary with opt 'n' and null names sets seq key")
	void testSetPrimarySeqKeyNullNames() {
		ds.setPrimary(null, "n");
		assertTrue(ds.isSeqKey());
		assertNull(ds.getPrimary());
	}

	@Test
	@DisplayName("setPrimary with opt 'n' and valid names sets seq key")
	void testSetPrimarySeqKeyWithNames() {
		ds.setPrimary(new String[]{"id"}, "n");
		assertTrue(ds.isSeqKey());
		assertEquals(0, ds.getTimeKeyCount());
	}

	@Test
	@DisplayName("setPrimary with opt 'tn' sets both time and seq key")
	void testSetPrimaryBothTimeAndSeqKey() {
		ds.setPrimary(new String[]{"id", "age"}, "tn");
		assertEquals(1, ds.getTimeKeyCount());
		assertTrue(ds.isSeqKey());
	}

	@Test
	@DisplayName("setPrimary resets sign when called again")
	void testSetPrimaryResetsSign() {
		ds.setPrimary(new String[]{"id"}, "t");
		assertEquals(1, ds.getTimeKeyCount());

		// Calling setPrimary again should reset sign to 0
		ds.setPrimary(new String[]{"id"});
		assertEquals(0, ds.getTimeKeyCount());
		assertFalse(ds.isSeqKey());
	}

	@Test
	@DisplayName("isSeqKey returns false when no seq key set")
	void testIsSeqKeyFalseByDefault() {
		assertFalse(ds.isSeqKey());
	}

	@Test
	@DisplayName("getTimeKeyCount returns 0 when no time key set")
	void testGetTimeKeyCountZeroByDefault() {
		assertEquals(0, ds.getTimeKeyCount());
	}

	// ===== getBaseKeyIndex tests =====

	@Test
	@DisplayName("getBaseKeyIndex returns full pkIndex when no time key")
	void testGetBaseKeyIndexNoTimeKey() {
		ds.setPrimary(new String[]{"id", "name"});
		assertArrayEquals(new int[]{0, 1}, ds.getBaseKeyIndex());
	}

	@Test
	@DisplayName("getBaseKeyIndex excludes last element when time key is set")
	void testGetBaseKeyIndexWithTimeKey() {
		ds.setPrimary(new String[]{"id", "name", "age"}, "t");
		int[] baseIndex = ds.getBaseKeyIndex();
		assertArrayEquals(new int[]{0, 1}, baseIndex);
	}

	@Test
	@DisplayName("getBaseKeyIndex returns null when no primary key set")
	void testGetBaseKeyIndexNoPrimary() {
		assertNull(ds.getBaseKeyIndex());
	}

	// ===== dup tests =====

	@Test
	@DisplayName("dup creates copy with same fields and primary key")
	void testDup() {
		ds.setPrimary(new String[]{"id"});
		DataStruct copy = ds.dup();

		assertNotSame(ds, copy);
		assertEquals(ds.getFieldCount(), copy.getFieldCount());
		for (int i = 0; i < ds.getFieldCount(); i++) {
			assertEquals(ds.getFieldName(i), copy.getFieldName(i));
		}
		assertArrayEquals(ds.getPrimary(), copy.getPrimary());
		assertArrayEquals(ds.getPKIndex(), copy.getPKIndex());
	}

	@Test
	@DisplayName("dup preserves time/seq key sign")
	void testDupPreservesSign() {
		ds.setPrimary(new String[]{"id", "age"}, "tn");
		DataStruct copy = ds.dup();
		assertEquals(1, copy.getTimeKeyCount());
		assertTrue(copy.isSeqKey());
	}

	@Test
	@DisplayName("dup copy is independent - modifying copy doesn't affect original")
	void testDupIndependence() {
		ds.setPrimary(new String[]{"id"});
		DataStruct copy = ds.dup();
		copy.setPrimary(null);

		assertNotNull(ds.getPrimary());
		assertNull(copy.getPrimary());
	}

	// ===== create tests =====

	@Test
	@DisplayName("create with all primary key fields preserves primary key and sign")
	void testCreateWithAllPKFields() {
		ds.setPrimary(new String[]{"id"}, "n");
		DataStruct created = ds.create(new String[]{"id", "name"});

		assertEquals(2, created.getFieldCount());
		assertArrayEquals(new String[]{"id"}, created.getPrimary());
		assertTrue(created.isSeqKey());
	}

	@Test
	@DisplayName("create drops primary key fields not present in new struct")
	void testCreateDropsMissingPKFields() {
		ds.setPrimary(new String[]{"id", "name"});
		DataStruct created = ds.create(new String[]{"id", "age"});

		assertArrayEquals(new String[]{"id"}, created.getPrimary());
	}

	@Test
	@DisplayName("create drops all primary keys when none are present in new struct")
	void testCreateDropsAllPKFields() {
		ds.setPrimary(new String[]{"id"});
		DataStruct created = ds.create(new String[]{"age", "name"});

		assertNull(created.getPrimary());
	}

	@Test
	@DisplayName("create without primary key on source sets no primary on result")
	void testCreateNoPrimaryKey() {
		DataStruct created = ds.create(new String[]{"age"});
		assertNull(created.getPrimary());
	}

	// ===== rename tests =====

	@Test
	@DisplayName("rename changes field name and returns correct updated names")
	void testRenameBasic() {
		ds.rename(new String[]{"name"}, new String[]{"fullName"});
		assertEquals("fullName", ds.getFieldName(1));
		assertEquals(1, ds.getFieldIndex("fullName"));
		assertEquals(-1, ds.getFieldIndex("name"));
	}

	@Test
	@DisplayName("rename updates primary key names when renamed field is a key")
	void testRenameUpdatesPrimaryKey() {
		ds.setPrimary(new String[]{"id"});
		ds.rename(new String[]{"id"}, new String[]{"userId"});

		assertEquals("userId", ds.getFieldName(0));
		assertArrayEquals(new String[]{"userId"}, ds.getPrimary());
	}

	@Test
	@DisplayName("rename with null newField auto-names to _n")
	void testRenameNullNewFieldAutoRenames() {
		ds.rename(new String[]{"name"}, new String[]{null});
		assertEquals("_2", ds.getFieldName(1));
	}

	@Test
	@DisplayName("rename ignores non-existent source fields")
	void testRenameIgnoresNonExistent() {
		ds.rename(new String[]{"nonexistent"}, new String[]{"whatever"});
		// Should not throw, fields remain unchanged
		assertEquals("id", ds.getFieldName(0));
		assertEquals("name", ds.getFieldName(1));
		assertEquals("age", ds.getFieldName(2));
	}

	@Test
	@DisplayName("rename with null srcFields does nothing")
	void testRenameNullSrcFields() {
		ds.rename(null, null);
		assertEquals("id", ds.getFieldName(0));
	}

	@Test
	@DisplayName("rename multiple fields at once")
	void testRenameMultipleFields() {
		ds.rename(new String[]{"id", "age"}, new String[]{"userId", "years"});
		assertEquals("userId", ds.getFieldName(0));
		assertEquals("name", ds.getFieldName(1));
		assertEquals("years", ds.getFieldName(2));
	}

	// ===== getFieldNames tests =====

	@Test
	@DisplayName("getFieldNames returns the internal field name array")
	void testGetFieldNames() {
		String[] names = ds.getFieldNames();
		assertArrayEquals(new String[]{"id", "name", "age"}, names);
		// It returns the same internal array, not a copy
		assertSame(names, ds.getFieldNames());
	}
}
