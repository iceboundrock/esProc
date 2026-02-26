package com.scudata.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("Area - rectangular region")
public class AreaTest {

    @Test
    void defaultConstructor() {
        Area a = new Area();
        assertEquals(-1, a.getBeginRow());
        assertEquals(-1, a.getEndRow());
        assertEquals(-1, a.getBeginCol());
        assertEquals(-1, a.getEndCol());
    }

    @Test
    void twoArgConstructorSetsRows() {
        Area a = new Area(1, 10);
        assertEquals(1, a.getBeginRow());
        assertEquals(10, a.getEndRow());
        assertEquals(-1, a.getBeginCol());
        assertEquals(-1, a.getEndCol());
    }

    @Test
    void fourArgConstructor() {
        Area a = new Area(1, 2, 10, 20);
        assertEquals(1, a.getBeginRow());
        assertEquals(2, a.getBeginCol());
        assertEquals(10, a.getEndRow());
        assertEquals(20, a.getEndCol());
    }

    @Test
    void setters() {
        Area a = new Area();
        a.setBeginRow(5);
        a.setBeginCol(3);
        a.setEndRow(10);
        a.setEndCol(8);
        assertEquals(5, a.getBeginRow());
        assertEquals(3, a.getBeginCol());
        assertEquals(10, a.getEndRow());
        assertEquals(8, a.getEndCol());
    }

    @Test
    void setArea() {
        Area a = new Area();
        a.setArea(1, 2, 3, 4);
        assertEquals(1, a.getBeginRow());
        assertEquals(2, a.getBeginCol());
        assertEquals(3, a.getEndRow());
        assertEquals(4, a.getEndCol());
    }

    @Test
    void containsPointInside() {
        Area a = new Area(1, 1, 10, 10);
        assertTrue(a.contains(5, 5));
        assertTrue(a.contains(1, 1));  // boundary
        assertTrue(a.contains(10, 10)); // boundary
    }

    @Test
    void containsPointOutside() {
        Area a = new Area(1, 1, 10, 10);
        assertFalse(a.contains(0, 5));
        assertFalse(a.contains(11, 5));
        assertFalse(a.contains(5, 0));
        assertFalse(a.contains(5, 11));
    }

    @Test
    void containsAreaInside() {
        Area outer = new Area(1, 1, 10, 10);
        Area inner = new Area(2, 2, 9, 9);
        assertTrue(outer.contains(inner));
    }

    @Test
    void containsAreaSame() {
        Area a = new Area(1, 1, 10, 10);
        Area b = new Area(1, 1, 10, 10);
        assertTrue(a.contains(b));
    }

    @Test
    void doesNotContainAreaPartiallyOutside() {
        Area outer = new Area(1, 1, 10, 10);
        Area inner = new Area(5, 5, 15, 15);
        assertFalse(outer.contains(inner));
    }

    @Test
    void compareToEqual() {
        Area a = new Area(1, 1, 10, 10);
        Area b = new Area(1, 1, 10, 10);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void compareToByBeginRow() {
        Area a = new Area(1, 1, 10, 10);
        Area b = new Area(2, 1, 10, 10);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    void compareToByBeginCol() {
        Area a = new Area(1, 1, 10, 10);
        Area b = new Area(1, 2, 10, 10);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    void compareToByEndRow() {
        Area a = new Area(1, 1, 10, 10);
        Area b = new Area(1, 1, 11, 10);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    void compareToByEndCol() {
        Area a = new Area(1, 1, 10, 10);
        Area b = new Area(1, 1, 10, 11);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    void deepClone() {
        Area a = new Area(1, 2, 3, 4);
        Area clone = (Area) a.deepClone();
        assertNotSame(a, clone);
        assertEquals(0, a.compareTo(clone));
        assertEquals(a.getBeginRow(), clone.getBeginRow());
        assertEquals(a.getBeginCol(), clone.getBeginCol());
    }

    @Test
    void serializeAndFillRecord() throws IOException, ClassNotFoundException {
        Area a = new Area(5, 10, 20, 30);
        byte[] data = a.serialize();
        Area restored = new Area();
        restored.fillRecord(data);
        assertEquals(5, restored.getBeginRow());
        assertEquals(10, restored.getBeginCol());  // note: serialize writes c1 after r2
        assertEquals(20, restored.getEndRow());
        assertEquals(30, restored.getEndCol());
    }
}
