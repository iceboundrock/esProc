package com.scudata.dm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for Param")
class ParamTest {

	@Test
	@DisplayName("Default constructor sets name null, kind VAR, value null")
	void testDefaultConstructor() {
		Param p = new Param();
		assertNull(p.getName());
		assertEquals(Param.VAR, p.getKind());
		assertNull(p.getValue());
	}

	@Test
	@DisplayName("Full constructor sets name, kind, and value")
	void testFullConstructor() {
		Param p = new Param("x", Param.ARG, 42);
		assertEquals("x", p.getName());
		assertEquals(Param.ARG, p.getKind());
		assertEquals(42, p.getValue());
	}

	@Test
	@DisplayName("Copy constructor copies name, kind, value, and editValue")
	void testCopyConstructor() {
		Param original = new Param("src", Param.CONST, "hello");
		original.setEditValue("edit1");

		Param copy = new Param(original);
		assertEquals("src", copy.getName());
		assertEquals(Param.CONST, copy.getKind());
		assertEquals("hello", copy.getValue());
		assertEquals("edit1", copy.getEditValue());
	}

	@Test
	@DisplayName("Copy constructor with null other does not throw NPE")
	void testCopyConstructorWithNull() {
		Param p = assertDoesNotThrow(() -> new Param((Param) null));
		assertNull(p.getName());
		assertEquals(Param.VAR, p.getKind());
		assertNull(p.getValue());
		assertNull(p.getEditValue());
	}

	@Test
	@DisplayName("getName and setName work correctly")
	void testGetSetName() {
		Param p = new Param();
		assertNull(p.getName());
		p.setName("alpha");
		assertEquals("alpha", p.getName());
		p.setName("beta");
		assertEquals("beta", p.getName());
	}

	@Test
	@DisplayName("getKind and setKind work correctly")
	void testGetSetKind() {
		Param p = new Param();
		assertEquals(Param.VAR, p.getKind());
		p.setKind(Param.ARG);
		assertEquals(Param.ARG, p.getKind());
		p.setKind(Param.CONST);
		assertEquals(Param.CONST, p.getKind());
	}

	@Test
	@DisplayName("getValue and setValue work correctly")
	void testGetSetValue() {
		Param p = new Param();
		assertNull(p.getValue());
		p.setValue(3.14);
		assertEquals(3.14, p.getValue());
		p.setValue("text");
		assertEquals("text", p.getValue());
	}

	@Test
	@DisplayName("getRemark and setRemark work correctly")
	void testGetSetRemark() {
		Param p = new Param();
		assertNull(p.getRemark());
		p.setRemark("some remark");
		assertEquals("some remark", p.getRemark());
	}

	@Test
	@DisplayName("getEditValue and setEditValue work correctly")
	void testGetSetEditValue() {
		Param p = new Param();
		assertNull(p.getEditValue());
		Object obj = new Object();
		p.setEditValue(obj);
		assertSame(obj, p.getEditValue());
	}

	@Test
	@DisplayName("isDeleted defaults to false")
	void testIsDeletedDefault() {
		Param p = new Param();
		assertFalse(p.isDeleted());
	}

	@Test
	@DisplayName("setDeleted changes the deleted flag")
	void testSetDeleted() {
		Param p = new Param();
		p.setDeleted(true);
		assertTrue(p.isDeleted());
		p.setDeleted(false);
		assertFalse(p.isDeleted());
	}

	@Test
	@DisplayName("deepClone creates an independent copy")
	void testDeepClone() {
		Param original = new Param("var1", Param.ARG, 100);
		original.setEditValue("ev");

		Param clone = (Param) original.deepClone();
		assertNotSame(original, clone);
		assertEquals(original.getName(), clone.getName());
		assertEquals(original.getKind(), clone.getKind());
		assertEquals(original.getValue(), clone.getValue());
		assertEquals(original.getEditValue(), clone.getEditValue());

		// Mutating clone does not affect original
		clone.setName("changed");
		clone.setValue(999);
		assertEquals("var1", original.getName());
		assertEquals(100, original.getValue());
	}

	@Test
	@DisplayName("deepClone does not copy remark (copy constructor does not copy remark)")
	void testDeepCloneDoesNotCopyRemark() {
		Param original = new Param("r", Param.VAR, null);
		original.setRemark("note");

		Param clone = (Param) original.deepClone();
		assertNull(clone.getRemark());
	}

	@Test
	@DisplayName("Constants VAR, ARG, CONST have correct values")
	void testConstants() {
		assertEquals(0, Param.VAR);
		assertEquals(1, Param.ARG);
		assertEquals(3, Param.CONST);
	}

	@Test
	@DisplayName("Full constructor with null name and null value is allowed")
	void testFullConstructorWithNulls() {
		Param p = new Param(null, Param.CONST, null);
		assertNull(p.getName());
		assertEquals(Param.CONST, p.getKind());
		assertNull(p.getValue());
	}
}
