package com.scudata.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("CellLocation - cell ID parsing and formatting")
public class CellLocationTest {

    @Test
    void parseA1() {
        CellLocation cl = CellLocation.parse("A1");
        assertNotNull(cl);
        assertEquals(1, cl.getRow());
        assertEquals(1, cl.getCol());
    }

    @Test
    void parseB10() {
        CellLocation cl = CellLocation.parse("B10");
        assertNotNull(cl);
        assertEquals(10, cl.getRow());
        assertEquals(2, cl.getCol());
    }

    @Test
    void parseZ99() {
        CellLocation cl = CellLocation.parse("Z99");
        assertNotNull(cl);
        assertEquals(99, cl.getRow());
        assertEquals(26, cl.getCol());
    }

    @Test
    void parseAA1() {
        CellLocation cl = CellLocation.parse("AA1");
        assertNotNull(cl);
        assertEquals(1, cl.getRow());
        assertEquals(27, cl.getCol()); // AA = 26 + 1
    }

    @Test
    void parseAZ100() {
        CellLocation cl = CellLocation.parse("AZ100");
        assertNotNull(cl);
        assertEquals(100, cl.getRow());
        assertEquals(52, cl.getCol()); // AZ = 26 + 26
    }

    @Test
    void parseNullReturnsNull() {
        assertNull(CellLocation.parse(null));
    }

    @Test
    void parseSingleCharReturnsNull() {
        assertNull(CellLocation.parse("A")); // no row number
    }

    @Test
    void parseInvalidReturnsNull() {
        assertNull(CellLocation.parse("123")); // no column letter
    }

    @Test
    void toColA() {
        assertEquals("A", CellLocation.toCol(1));
    }

    @Test
    void toColZ() {
        assertEquals("Z", CellLocation.toCol(26));
    }

    @Test
    void toColAA() {
        assertEquals("AA", CellLocation.toCol(27));
    }

    @Test
    void toColNegative() {
        assertNull(CellLocation.toCol(-1));
    }

    @Test
    void toRow() {
        assertEquals("1", CellLocation.toRow(1));
        assertEquals("100", CellLocation.toRow(100));
        assertEquals("0", CellLocation.toRow(0));
    }

    @Test
    void toRowNegative() {
        assertNull(CellLocation.toRow(-1));
    }

    @Test
    void parseCol() {
        assertEquals(1, CellLocation.parseCol("A"));
        assertEquals(26, CellLocation.parseCol("Z"));
        assertEquals(27, CellLocation.parseCol("AA"));
        assertEquals(-1, CellLocation.parseCol(null));
        assertEquals(-1, CellLocation.parseCol(""));
        assertEquals(-1, CellLocation.parseCol("a")); // lowercase
    }

    @Test
    void parseRow() {
        assertEquals(1, CellLocation.parseRow("1"));
        assertEquals(100, CellLocation.parseRow("100"));
        assertEquals(-1, CellLocation.parseRow(null));
        assertEquals(-1, CellLocation.parseRow(""));
        assertEquals(-1, CellLocation.parseRow("abc"));
    }

    @Test
    void getCellId() {
        assertEquals("A1", CellLocation.getCellId(1, 1));
        assertEquals("B10", CellLocation.getCellId(10, 2));
        assertEquals("Z99", CellLocation.getCellId(99, 26));
    }

    @Test
    void getCellIdNegativeRow() {
        assertNull(CellLocation.getCellId(-1, 1));
    }

    @Test
    void getCellIdNegativeCol() {
        assertNull(CellLocation.getCellId(1, -1));
    }

    @Test
    void toStringFormat() {
        CellLocation cl = new CellLocation(5, 3); // row=5, col=3=C
        assertEquals("C5", cl.toString());
    }

    @Test
    void constructorFromString() {
        CellLocation cl = new CellLocation("B10");
        assertEquals(10, cl.getRow());
        assertEquals(2, cl.getCol());
    }

    @Test
    void copyConstructor() {
        CellLocation original = new CellLocation(3, 5);
        CellLocation copy = new CellLocation(original);
        assertEquals(original.getRow(), copy.getRow());
        assertEquals(original.getCol(), copy.getCol());
    }

    @Test
    void equalsAndHashCode() {
        CellLocation a = new CellLocation(1, 1);
        CellLocation b = new CellLocation(1, 1);
        CellLocation c = new CellLocation(2, 1);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertEquals(a, a); // same reference
        assertNotEquals(a, "not a CellLocation");
    }

    @Test
    void setters() {
        CellLocation cl = new CellLocation();
        cl.setRow(10);
        cl.setCol(5);
        assertEquals(10, cl.getRow());
        assertEquals(5, cl.getCol());
    }

    @Test
    void setRowAndCol() {
        CellLocation cl = new CellLocation();
        cl.set(10, 5);
        assertEquals(10, cl.getRow());
        assertEquals(5, cl.getCol());
    }

    @Test
    void serializeAndFillRecord() throws IOException {
        CellLocation original = new CellLocation(42, 7);
        byte[] data = original.serialize();
        CellLocation restored = new CellLocation();
        restored.fillRecord(data);
        assertEquals(42, restored.getRow());
        assertEquals(7, restored.getCol());
    }

    @Test
    void parseAndToStringRoundTrip() {
        String[] ids = {"A1", "B10", "Z99", "AA1", "AZ100"};
        for (String id : ids) {
            CellLocation cl = CellLocation.parse(id);
            assertNotNull(cl, "Failed to parse: " + id);
            assertEquals(id, cl.toString(), "Round-trip failed for: " + id);
        }
    }
}
