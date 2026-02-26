package com.scudata.dm;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

@DisplayName("Tests for Context")
class ContextTest {

	private Context ctx;

	@BeforeEach
	void setUp() {
		ctx = new Context();
	}

	@Test
	@DisplayName("Default constructor initializes paramList, computeStack, and iterateParam")
	void testDefaultConstructor() {
		assertNotNull(ctx.getParamList());
		assertEquals(0, ctx.getParamList().count());
		assertNotNull(ctx.getComputeStack());
		assertNotNull(ctx.getIterateParam());
		assertNull(ctx.getParent());
		assertNull(ctx.getJobSpace());
		assertNull(ctx.getDefDBsessionName());
	}

	@Test
	@DisplayName("Parent constructor sets parent and inherits JobSpace")
	void testParentConstructor() {
		Context parent = new Context();
		JobSpace js = new JobSpace("testJob");
		parent.setJobSpace(js);

		Context child = new Context(parent);
		assertSame(parent, child.getParent());
		assertSame(js, child.getJobSpace());
	}

	@Test
	@DisplayName("setParent sets parent and inherits JobSpace; setParent(null) clears parent")
	void testSetParentAndClear() {
		Context parent = new Context();
		JobSpace js = new JobSpace("job1");
		parent.setJobSpace(js);

		ctx.setParent(parent);
		assertSame(parent, ctx.getParent());
		assertSame(js, ctx.getJobSpace());

		ctx.setParent(null);
		assertNull(ctx.getParent());
		// JobSpace is NOT cleared when parent is set to null
		assertSame(js, ctx.getJobSpace());
	}

	@Test
	@DisplayName("getParam checks local paramList first, then parent recursively")
	void testGetParamLocalThenParent() {
		Context parent = new Context();
		parent.addParam(new Param("shared", Param.VAR, "parentValue"));
		parent.addParam(new Param("parentOnly", Param.VAR, "fromParent"));

		ctx.setParent(parent);
		ctx.addParam(new Param("shared", Param.VAR, "childValue"));
		ctx.addParam(new Param("childOnly", Param.VAR, "fromChild"));

		// Local param shadows parent param with same name
		assertEquals("childValue", ctx.getParam("shared").getValue());
		// Falls through to parent when not found locally
		assertEquals("fromParent", ctx.getParam("parentOnly").getValue());
		// Local-only param found
		assertEquals("fromChild", ctx.getParam("childOnly").getValue());
		// Non-existent param returns null
		assertNull(ctx.getParam("noSuchParam"));
	}

	@Test
	@DisplayName("getParam returns null for non-existent param without parent")
	void testGetParamNoParent() {
		assertNull(ctx.getParam("missing"));
	}

	@Test
	@DisplayName("addParam adds to local paramList")
	void testAddParam() {
		assertEquals(0, ctx.getParamList().count());
		Param p = new Param("x", Param.ARG, 10);
		ctx.addParam(p);
		assertEquals(1, ctx.getParamList().count());
		assertSame(p, ctx.getParam("x"));
	}

	@Test
	@DisplayName("removeParam removes from local list and sets deleted flag")
	void testRemoveParamSetsDeleted() {
		Param p = new Param("toRemove", Param.VAR, "val");
		ctx.addParam(p);
		assertFalse(p.isDeleted());

		Param removed = ctx.removeParam("toRemove");
		assertSame(p, removed);
		assertTrue(removed.isDeleted());
		assertNull(ctx.getParam("toRemove"));
		assertEquals(0, ctx.getParamList().count());
	}

	@Test
	@DisplayName("removeParam does NOT remove from parent context")
	void testRemoveParamDoesNotAffectParent() {
		Context parent = new Context();
		Param pp = new Param("inherited", Param.VAR, "pVal");
		parent.addParam(pp);

		ctx.setParent(parent);
		// Param is visible through parent lookup
		assertNotNull(ctx.getParam("inherited"));

		// removeParam on child only looks at child's local paramList
		Param result = ctx.removeParam("inherited");
		assertNull(result); // not found in child's local list
		assertFalse(pp.isDeleted());
		// Still accessible through parent
		assertNotNull(ctx.getParam("inherited"));
	}

	@Test
	@DisplayName("setParamValue updates existing param value")
	void testSetParamValueUpdatesExisting() {
		ctx.addParam(new Param("v", Param.ARG, "old"));
		ctx.setParamValue("v", "new");
		assertEquals("new", ctx.getParam("v").getValue());
		// Kind should remain ARG (not changed to VAR)
		assertEquals(Param.ARG, ctx.getParam("v").getKind());
	}

	@Test
	@DisplayName("setParamValue creates new VAR param when not found")
	void testSetParamValueCreatesNewVar() {
		ctx.setParamValue("fresh", 42);
		Param p = ctx.getParam("fresh");
		assertNotNull(p);
		assertEquals(42, p.getValue());
		assertEquals(Param.VAR, p.getKind());
	}

	@Test
	@DisplayName("setParamValue with paramType creates new param with specified type")
	void testSetParamValueWithParamType() {
		ctx.setParamValue("typed", "argVal", Param.ARG);
		Param p = ctx.getParam("typed");
		assertNotNull(p);
		assertEquals("argVal", p.getValue());
		assertEquals(Param.ARG, p.getKind());
	}

	@Test
	@DisplayName("setParamValue with paramType updates existing param ignoring specified type")
	void testSetParamValueWithParamTypeUpdatesExisting() {
		ctx.addParam(new Param("existing", Param.CONST, "orig"));
		ctx.setParamValue("existing", "updated", Param.ARG);
		Param p = ctx.getParam("existing");
		assertEquals("updated", p.getValue());
		// Type should remain CONST; setParamValue only sets value on existing params
		assertEquals(Param.CONST, p.getKind());
	}

	@Test
	@DisplayName("getComputeStack returns non-null stack, same instance each call")
	void testGetComputeStack() {
		ComputeStack stack1 = ctx.getComputeStack();
		ComputeStack stack2 = ctx.getComputeStack();
		assertNotNull(stack1);
		assertSame(stack1, stack2);
	}

	@Test
	@DisplayName("getRandom() lazy-creates a Random instance")
	void testGetRandomLazyCreation() {
		Random r1 = ctx.getRandom();
		assertNotNull(r1);
		Random r2 = ctx.getRandom();
		assertSame(r1, r2);
	}

	@Test
	@DisplayName("getRandom(seed) creates seeded Random, re-seeds on second call")
	void testGetRandomWithSeed() {
		Random r1 = ctx.getRandom(12345L);
		assertNotNull(r1);

		// Same instance should be returned on second call (re-seeded)
		Random r2 = ctx.getRandom(99999L);
		assertSame(r1, r2);

		// After getRandom(seed), getRandom() returns the same instance
		Random r3 = ctx.getRandom();
		assertSame(r1, r3);
	}

	@Test
	@DisplayName("getRandom(seed) produces deterministic sequence")
	void testGetRandomDeterministic() {
		Random r1 = ctx.getRandom(42L);
		int val1 = r1.nextInt();

		// Re-seed and verify same value
		ctx.getRandom(42L);
		int val2 = r1.nextInt();
		assertEquals(val1, val2);
	}

	@Test
	@DisplayName("setParamList with null creates new empty ParamList")
	void testSetParamListNull() {
		ctx.addParam(new Param("a", Param.VAR, 1));
		assertEquals(1, ctx.getParamList().count());

		ctx.setParamList(null);
		assertNotNull(ctx.getParamList());
		assertEquals(0, ctx.getParamList().count());
	}

	@Test
	@DisplayName("setParamList replaces the existing param list")
	void testSetParamList() {
		ParamList pl = new ParamList();
		pl.add(new Param("p1", Param.VAR, "v1"));
		pl.add(new Param("p2", Param.ARG, "v2"));
		ctx.setParamList(pl);

		assertSame(pl, ctx.getParamList());
		assertEquals(2, ctx.getParamList().count());
		assertNotNull(ctx.getParam("p1"));
		assertNotNull(ctx.getParam("p2"));
	}

	@Test
	@DisplayName("newComputeContext creates child with copied params and shared references")
	void testNewComputeContext() {
		ctx.addParam(new Param("a", Param.VAR, 100));
		ctx.addParam(new Param("b", Param.ARG, "hello"));
		ctx.setDefDBsessionName("myDB");

		Context child = ctx.newComputeContext();

		// Parent is set
		assertSame(ctx, child.getParent());

		// Params are copied (not shared)
		assertEquals(2, child.getParamList().count());
		Param childA = child.getParamList().get("a");
		assertNotNull(childA);
		assertEquals(100, childA.getValue());
		// Should be a different Param instance (copied via new Param(p))
		assertNotSame(ctx.getParamList().get("a"), childA);

		// defDsName is copied
		assertEquals("myDB", child.getDefDBsessionName());

		// dbSessions map is shared (same reference)
		assertSame(ctx.getDBSessionMap(), child.getDBSessionMap());

		// dbsfs map is shared (same reference)
		assertSame(ctx.getDBSessionFactoryMap(), child.getDBSessionFactoryMap());
	}

	@Test
	@DisplayName("setEnv copies js, dbSessions, dbsfs, defDsName but not params")
	void testSetEnv() {
		Context source = new Context();
		source.setDefDBsessionName("srcDB");
		JobSpace js = new JobSpace("envJob");
		source.setJobSpace(js);
		source.addParam(new Param("srcParam", Param.VAR, "srcVal"));

		ctx.addParam(new Param("localParam", Param.VAR, "localVal"));
		ctx.setEnv(source);

		// Environment properties copied
		assertEquals("srcDB", ctx.getDefDBsessionName());
		assertSame(js, ctx.getJobSpace());
		assertSame(source.getDBSessionMap(), ctx.getDBSessionMap());
		assertSame(source.getDBSessionFactoryMap(), ctx.getDBSessionFactoryMap());

		// Params are NOT copied
		assertNull(ctx.getParam("srcParam"));
		assertNotNull(ctx.getParam("localParam"));
	}

	@Test
	@DisplayName("getIterateParam returns param named ITERATEPARAM with VAR kind")
	void testGetIterateParam() {
		Param ip = ctx.getIterateParam();
		assertNotNull(ip);
		assertEquals(KeyWord.ITERATEPARAM, ip.getName());
		assertEquals(Param.VAR, ip.getKind());
		assertNull(ip.getValue());

		// Setting value on iterate param works
		ip.setValue(999);
		assertEquals(999, ctx.getIterateParam().getValue());
	}

	@Test
	@DisplayName("setDefDBsessionName and getDefDBsessionName round-trip")
	void testDefDBsessionName() {
		assertNull(ctx.getDefDBsessionName());
		ctx.setDefDBsessionName("db1");
		assertEquals("db1", ctx.getDefDBsessionName());
		ctx.setDefDBsessionName(null);
		assertNull(ctx.getDefDBsessionName());
	}
}
