package com.scudata.dm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for ParamList")
class ParamListTest {

	private ParamList list;

	@BeforeEach
	void setUp() {
		list = new ParamList();
	}

	@Test
	@DisplayName("New ParamList has count 0")
	void testNewParamListCountZero() {
		assertEquals(0, list.count());
	}

	@Test
	@DisplayName("add(Param) adds a param to the list")
	void testAddParam() {
		Param p = new Param("a", Param.VAR, 1);
		list.add(p);
		assertEquals(1, list.count());
		assertSame(p, list.get(0));
	}

	@Test
	@DisplayName("add(int, Param) inserts param at the given position")
	void testAddAtIndex() {
		list.add(new Param("a", Param.VAR, 1));
		list.add(new Param("c", Param.VAR, 3));

		Param middle = new Param("b", Param.VAR, 2);
		list.add(1, middle);

		assertEquals(3, list.count());
		assertSame(middle, list.get(1));
		assertEquals("a", list.get(0).getName());
		assertEquals("c", list.get(2).getName());
	}

	@Test
	@DisplayName("add(String, byte, Object) creates and adds a Param")
	void testAddByFields() {
		list.add("x", Param.ARG, 10);
		assertEquals(1, list.count());
		assertEquals("x", list.get(0).getName());
		assertEquals(Param.ARG, list.get(0).getKind());
		assertEquals(10, list.get(0).getValue());
	}

	@Test
	@DisplayName("add(String, byte, Object) rejects duplicate name")
	void testAddByFieldsDuplicateRejected() {
		list.add("dup", Param.VAR, 1);
		list.add("dup", Param.ARG, 2);
		assertEquals(1, list.count());
	}

	@Test
	@DisplayName("add(Param) does not call isValid — allows adding a param even with null name")
	void testAddParamNoValidation() {
		Param p = new Param(); // name is null
		list.add(p);
		assertEquals(1, list.count());
		assertSame(p, list.get(0));
	}

	@Test
	@DisplayName("add(int, Param) rejects null param via isValid")
	void testAddAtIndexRejectsNull() {
		list.add(0, null);
		assertEquals(0, list.count());
	}

	@Test
	@DisplayName("add(int, Param) rejects param with null name via isValid")
	void testAddAtIndexRejectsNullName() {
		list.add(0, new Param());
		assertEquals(0, list.count());
	}

	@Test
	@DisplayName("addVariable adds a VAR param")
	void testAddVariable() {
		list.addVariable("v1", "val");
		assertEquals(1, list.count());
		assertEquals(Param.VAR, list.get(0).getKind());
		assertEquals("val", list.get(0).getValue());
	}

	@Test
	@DisplayName("addArgument adds an ARG param")
	void testAddArgument() {
		list.addArgument("a1", 42);
		assertEquals(1, list.count());
		assertEquals(Param.ARG, list.get(0).getKind());
	}

	@Test
	@DisplayName("addConstant adds a CONST param")
	void testAddConstant() {
		list.addConstant("c1", 99);
		assertEquals(1, list.count());
		assertEquals(Param.CONST, list.get(0).getKind());
	}

	@Test
	@DisplayName("remove(int) removes existing param and returns it")
	void testRemoveByIndex() {
		Param p = new Param("r", Param.VAR, 5);
		list.add(p);
		Param removed = list.remove(0);
		assertSame(p, removed);
		assertEquals(0, list.count());
	}

	@Test
	@DisplayName("remove(int) returns null for out of range index")
	void testRemoveByIndexOutOfRange() {
		assertNull(list.remove(0));
		list.add(new Param("a", Param.VAR, 1));
		assertNull(list.remove(5));
	}

	@Test
	@DisplayName("remove(String) removes and returns param with matching name")
	void testRemoveByName() {
		Param p = new Param("target", Param.ARG, 10);
		list.add(p);
		list.add(new Param("other", Param.VAR, 20));

		Param removed = list.remove("target");
		assertSame(p, removed);
		assertEquals(1, list.count());
	}

	@Test
	@DisplayName("remove(String) returns null when name not found")
	void testRemoveByNameNotFound() {
		assertNull(list.remove("nonexistent"));
		list.add(new Param("a", Param.VAR, 1));
		assertNull(list.remove("b"));
	}

	@Test
	@DisplayName("get(int) returns param at index or null if out of range")
	void testGetByIndex() {
		assertNull(list.get(0));
		Param p = new Param("g", Param.VAR, 7);
		list.add(p);
		assertSame(p, list.get(0));
		assertNull(list.get(1));
		assertNull(list.get(100));
	}

	@Test
	@DisplayName("get(String) returns param by name or null if not found")
	void testGetByName() {
		assertNull(list.get("missing"));
		Param p = new Param("found", Param.CONST, "data");
		list.add(p);
		assertSame(p, list.get("found"));
		assertNull(list.get("notfound"));
	}

	@Test
	@DisplayName("getByValue uses identity (==) not equals")
	void testGetByValueIdentity() {
		String val = new String("hello");
		String sameContent = new String("hello");
		Param p = new Param("p1", Param.VAR, val);
		list.add(p);

		assertSame(p, list.getByValue(val));
		// Different object with same content should not match
		assertNull(list.getByValue(sameContent));
	}

	@Test
	@DisplayName("getByValue returns null on empty list")
	void testGetByValueEmptyList() {
		assertNull(list.getByValue("anything"));
	}

	@Test
	@DisplayName("count returns 0 after clear")
	void testCountAndClear() {
		list.add(new Param("a", Param.VAR, 1));
		list.add(new Param("b", Param.VAR, 2));
		assertEquals(2, list.count());

		list.clear();
		assertEquals(0, list.count());
	}

	@Test
	@DisplayName("contains returns true for present param and false otherwise")
	void testContains() {
		Param p = new Param("c", Param.VAR, 3);
		assertFalse(list.contains(p));
		list.add(p);
		assertTrue(list.contains(p));

		Param other = new Param("c", Param.VAR, 3);
		assertFalse(list.contains(other)); // different instance
	}

	@Test
	@DisplayName("getAllVarParams filters only VAR params")
	void testGetAllVarParams() {
		list.add(new Param("v1", Param.VAR, 1));
		list.add(new Param("a1", Param.ARG, 2));
		list.add(new Param("v2", Param.VAR, 3));
		list.add(new Param("c1", Param.CONST, 4));

		ParamList vars = new ParamList();
		list.getAllVarParams(vars);
		assertEquals(2, vars.count());
		assertEquals("v1", vars.get(0).getName());
		assertEquals("v2", vars.get(1).getName());
	}

	@Test
	@DisplayName("getAllArguments filters only ARG params")
	void testGetAllArguments() {
		list.add(new Param("v1", Param.VAR, 1));
		list.add(new Param("a1", Param.ARG, 2));
		list.add(new Param("a2", Param.ARG, 3));

		ParamList args = new ParamList();
		list.getAllArguments(args);
		assertEquals(2, args.count());
		assertEquals("a1", args.get(0).getName());
		assertEquals("a2", args.get(1).getName());
	}

	@Test
	@DisplayName("getAllConsts filters only CONST params")
	void testGetAllConsts() {
		list.add(new Param("v1", Param.VAR, 1));
		list.add(new Param("c1", Param.CONST, 2));
		list.add(new Param("c2", Param.CONST, 3));

		ParamList consts = new ParamList();
		list.getAllConsts(consts);
		assertEquals(2, consts.count());
		assertEquals("c1", consts.get(0).getName());
		assertEquals("c2", consts.get(1).getName());
	}

	@Test
	@DisplayName("getAllVarParams on empty list adds nothing")
	void testGetAllVarParamsEmpty() {
		ParamList vars = new ParamList();
		list.getAllVarParams(vars);
		assertEquals(0, vars.count());
	}

	@Test
	@DisplayName("addAll merges params from another ParamList")
	void testAddAll() {
		list.add(new Param("a", Param.VAR, 1));

		ParamList other = new ParamList();
		other.add(new Param("b", Param.ARG, 2));
		other.add(new Param("c", Param.CONST, 3));

		list.addAll(other);
		assertEquals(3, list.count());
		assertEquals("b", list.get(1).getName());
		assertEquals("c", list.get(2).getName());
	}

	@Test
	@DisplayName("addAll into empty list copies from source")
	void testAddAllIntoEmpty() {
		ParamList source = new ParamList();
		source.add(new Param("s1", Param.VAR, 10));

		list.addAll(source);
		assertEquals(1, list.count());
		assertEquals("s1", list.get(0).getName());
	}

	@Test
	@DisplayName("deepClone creates an independent copy of list and params")
	void testDeepClone() {
		list.add(new Param("d1", Param.VAR, 100));
		list.add(new Param("d2", Param.ARG, 200));
		list.setUserChangeable(true);

		ParamList clone = (ParamList) list.deepClone();
		assertNotSame(list, clone);
		assertEquals(list.count(), clone.count());
		assertTrue(clone.isUserChangeable());

		// Params are deep cloned (different instances)
		assertNotSame(list.get(0), clone.get(0));
		assertEquals("d1", clone.get(0).getName());
		assertEquals(100, clone.get(0).getValue());

		// Mutating clone does not affect original
		clone.get(0).setName("modified");
		assertEquals("d1", list.get(0).getName());
	}

	@Test
	@DisplayName("deepClone on empty list returns empty clone")
	void testDeepCloneEmpty() {
		ParamList clone = (ParamList) list.deepClone();
		assertNotSame(list, clone);
		assertEquals(0, clone.count());
	}

	@Test
	@DisplayName("isUserChangeable defaults to false")
	void testIsUserChangeableDefault() {
		assertFalse(list.isUserChangeable());
	}

	@Test
	@DisplayName("setUserChangeable toggles the flag")
	void testSetUserChangeable() {
		list.setUserChangeable(true);
		assertTrue(list.isUserChangeable());
		list.setUserChangeable(false);
		assertFalse(list.isUserChangeable());
	}
}
