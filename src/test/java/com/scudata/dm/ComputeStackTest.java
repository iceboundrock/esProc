package com.scudata.dm;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.NoSuchElementException;

/**
 * Tests for {@link ComputeStack}.
 */
class ComputeStackTest {

	private ComputeStack stack;
	private Sequence seq1;
	private Sequence seq2;
	private Current current1;
	private Current current2;

	@BeforeEach
	void setUp() {
		stack = new ComputeStack();

		seq1 = new Sequence(3);
		seq1.add(1);
		seq1.add(2);
		seq1.add(3);

		seq2 = new Sequence(2);
		seq2.add("a");
		seq2.add("b");

		current1 = new Current(seq1);
		current2 = new Current(seq2);
	}

	// ======================== Main stack tests ========================

	@Test
	@DisplayName("isStackEmpty returns true for a new stack")
	void isStackEmpty_newStack_returnsTrue() {
		assertTrue(stack.isStackEmpty());
	}

	@Test
	@DisplayName("push then isStackEmpty returns false")
	void push_makesStackNonEmpty() {
		stack.push(current1);
		assertFalse(stack.isStackEmpty());
	}

	@Test
	@DisplayName("push and getTopObject returns the pushed item")
	void push_getTopObject_returnsPushedItem() {
		stack.push(current1);
		assertSame(current1, stack.getTopObject());
	}

	@Test
	@DisplayName("push multiple items, getTopObject returns last pushed")
	void pushMultiple_getTopObject_returnsLastPushed() {
		stack.push(current1);
		stack.push(current2);
		assertSame(current2, stack.getTopObject());
	}

	@Test
	@DisplayName("pop removes the top item and reveals the one below")
	void pop_removesTopItem() {
		stack.push(current1);
		stack.push(current2);

		stack.pop();
		assertSame(current1, stack.getTopObject());

		stack.pop();
		assertTrue(stack.isStackEmpty());
	}

	@Test
	@DisplayName("pop calls popStack on the item being removed")
	void pop_callsPopStackOnItem() {
		stack.push(current1);
		// Before pop, current1 should report isInStack == true
		assertTrue(current1.isInStack(stack));

		stack.pop();
		// After pop, Current.popStack() sets isInStack to false
		assertFalse(current1.isInStack(stack));
	}

	@Test
	@DisplayName("pop on empty stack throws NoSuchElementException")
	void pop_emptyStack_throwsException() {
		assertThrows(NoSuchElementException.class, () -> stack.pop());
	}

	@Test
	@DisplayName("getTopObject on empty stack throws NoSuchElementException")
	void getTopObject_emptyStack_throwsException() {
		assertThrows(NoSuchElementException.class, () -> stack.getTopObject());
	}

	// ======================== isInComputeStack tests ========================

	@Test
	@DisplayName("isInComputeStack returns true for a pushed item and false for an unpushed item")
	void isInComputeStack_identityCheck() {
		stack.push(current1);

		assertTrue(stack.isInComputeStack(current1));
		assertFalse(stack.isInComputeStack(current2));
	}

	@Test
	@DisplayName("isInComputeStack uses identity comparison, not equals")
	void isInComputeStack_usesIdentity() {
		// Create a different Current wrapping the same sequence
		Current anotherCurrent = new Current(seq1);
		stack.push(current1);

		// Same data, different object — should return false
		assertFalse(stack.isInComputeStack(anotherCurrent));
		assertTrue(stack.isInComputeStack(current1));
	}

	@Test
	@DisplayName("isInComputeStack returns false on empty stack")
	void isInComputeStack_emptyStack_returnsFalse() {
		assertFalse(stack.isInComputeStack(current1));
	}

	// ======================== getStackHeadEntry tests ========================

	@Test
	@DisplayName("getStackHeadEntry returns null for empty stack and the head entry after push")
	void getStackHeadEntry_reflectsStackState() {
		assertNull(stack.getStackHeadEntry());

		stack.push(current1);
		LinkEntry<IComputeItem> entry = stack.getStackHeadEntry();
		assertNotNull(entry);
		assertSame(current1, entry.getElement());

		stack.push(current2);
		entry = stack.getStackHeadEntry();
		assertSame(current2, entry.getElement());
		// The next entry should point to current1
		assertNotNull(entry.getNext());
		assertSame(current1, entry.getNext().getElement());
	}

	// ======================== Arg stack tests ========================

	@Test
	@DisplayName("pushArg with non-null sequence, then getArg returns a Current wrapping it")
	void pushArg_nonNull_getArgReturnsCurrent() {
		stack.pushArg(seq1);

		Current arg = stack.getArg();
		assertNotNull(arg);
		assertSame(seq1, arg.getCurrentSequence());
	}

	@Test
	@DisplayName("pushArg with null sequence, then getArg returns null")
	void pushArg_null_getArgReturnsNull() {
		stack.pushArg(null);

		// A null Current is pushed
		assertNull(stack.getArg());
	}

	@Test
	@DisplayName("popArg removes the top arg entry")
	void popArg_removesTopArg() {
		stack.pushArg(seq1);
		stack.pushArg(seq2);

		Current top = stack.getArg();
		assertSame(seq2, top.getCurrentSequence());

		stack.popArg();
		Current next = stack.getArg();
		assertSame(seq1, next.getCurrentSequence());

		stack.popArg();
		// Now the arg stack is empty
		assertThrows(NoSuchElementException.class, () -> stack.getArg());
	}

	@Test
	@DisplayName("popArg on empty arg stack throws NoSuchElementException")
	void popArg_emptyStack_throwsException() {
		assertThrows(NoSuchElementException.class, () -> stack.popArg());
	}

	@Test
	@DisplayName("getArg on empty arg stack throws NoSuchElementException")
	void getArg_emptyStack_throwsException() {
		assertThrows(NoSuchElementException.class, () -> stack.getArg());
	}

	// ======================== Clear / Reset tests ========================

	@Test
	@DisplayName("clearStackList clears only the main stack, arg stack is unaffected")
	void clearStackList_clearsMainStackOnly() {
		stack.push(current1);
		stack.pushArg(seq1);

		stack.clearStackList();

		assertTrue(stack.isStackEmpty());
		// Arg stack should still have its entry
		assertNotNull(stack.getArg());
	}

	@Test
	@DisplayName("clearArgStackList clears only the arg stack, main stack is unaffected")
	void clearArgStackList_clearsArgStackOnly() {
		stack.push(current1);
		stack.pushArg(seq1);

		stack.clearArgStackList();

		assertFalse(stack.isStackEmpty());
		assertSame(current1, stack.getTopObject());
		assertThrows(NoSuchElementException.class, () -> stack.getArg());
	}

	@Test
	@DisplayName("reset clears both main stack and arg stack")
	void reset_clearsBothStacks() {
		stack.push(current1);
		stack.push(current2);
		stack.pushArg(seq1);
		stack.pushArg(seq2);

		stack.reset();

		assertTrue(stack.isStackEmpty());
		assertThrows(NoSuchElementException.class, () -> stack.getTopObject());
		assertThrows(NoSuchElementException.class, () -> stack.getArg());
	}

	// ======================== getCurrentValue / getCurrentIndex / getSequenceCurrent / getTopSequence / getTopCurrent tests ========================

	@Test
	@DisplayName("getCurrentValue returns element value when sequence is on stack")
	void getCurrentValue_sequenceOnStack() {
		current1.setCurrent(2);
		stack.push(current1);

		Object val = stack.getCurrentValue(seq1);
		assertEquals(2, val);
	}

	@Test
	@DisplayName("getCurrentValue returns first element when sequence is not on stack and non-empty")
	void getCurrentValue_sequenceNotOnStack_returnsFirst() {
		// seq1 is not on the stack
		Object val = stack.getCurrentValue(seq1);
		assertEquals(1, val);
	}

	@Test
	@DisplayName("getCurrentValue returns null when sequence is not on stack and empty")
	void getCurrentValue_emptySequenceNotOnStack_returnsNull() {
		Sequence emptySeq = new Sequence(0);
		assertNull(stack.getCurrentValue(emptySeq));
	}

	@Test
	@DisplayName("getCurrentIndex returns the current index when sequence is on stack")
	void getCurrentIndex_sequenceOnStack() {
		current1.setCurrent(3);
		stack.push(current1);

		assertEquals(3, stack.getCurrentIndex(seq1));
	}

	@Test
	@DisplayName("getCurrentIndex returns 0 when sequence is not on stack")
	void getCurrentIndex_sequenceNotOnStack_returnsZero() {
		assertEquals(0, stack.getCurrentIndex(seq1));
	}

	@Test
	@DisplayName("getSequenceCurrent returns the matching Current or null")
	void getSequenceCurrent_matchAndMiss() {
		stack.push(current1);

		assertSame(current1, stack.getSequenceCurrent(seq1));
		assertNull(stack.getSequenceCurrent(seq2));
	}

	@Test
	@DisplayName("getTopSequence returns the sequence of the top Current on the stack")
	void getTopSequence_returnsMostRecentSequence() {
		stack.push(current1);
		stack.push(current2);

		assertSame(seq2, stack.getTopSequence());
	}

	@Test
	@DisplayName("getTopSequence throws NoSuchElementException on empty stack")
	void getTopSequence_emptyStack_throwsException() {
		assertThrows(NoSuchElementException.class, () -> stack.getTopSequence());
	}

	@Test
	@DisplayName("getTopCurrent returns the topmost Current instance")
	void getTopCurrent_returnsTopmostCurrent() {
		stack.push(current1);
		stack.push(current2);

		assertSame(current2, stack.getTopCurrent());
	}

	@Test
	@DisplayName("getTopCurrent throws NoSuchElementException on empty stack")
	void getTopCurrent_emptyStack_throwsException() {
		assertThrows(NoSuchElementException.class, () -> stack.getTopCurrent());
	}

	// ======================== Multiple push/pop sequence ========================

	@Test
	@DisplayName("Multiple push/pop operations maintain correct LIFO ordering")
	void multiplePushPop_maintainsLifoOrder() {
		Sequence seq3 = new Sequence(1);
		seq3.add(99);
		Current current3 = new Current(seq3);

		stack.push(current1);
		stack.push(current2);
		stack.push(current3);

		assertSame(current3, stack.getTopObject());
		assertTrue(stack.isInComputeStack(current1));
		assertTrue(stack.isInComputeStack(current2));
		assertTrue(stack.isInComputeStack(current3));

		stack.pop();
		assertSame(current2, stack.getTopObject());
		assertFalse(stack.isInComputeStack(current3));

		stack.pop();
		assertSame(current1, stack.getTopObject());

		stack.pop();
		assertTrue(stack.isStackEmpty());
	}
}
